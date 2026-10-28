package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.BbsLike;
import com.ruoyi.system.domain.BbsDislike;
import com.ruoyi.system.domain.BbsComment;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.mapper.BbsLikeMapper;
import com.ruoyi.system.mapper.BbsDislikeMapper;
import com.ruoyi.system.mapper.BbsCommentMapper;
import com.ruoyi.system.mapper.BbsPostMapper;
import com.ruoyi.system.service.IBbsCommentService;
import com.ruoyi.system.service.IBbsNotificationService;

@Service
public class BbsCommentServiceImpl implements IBbsCommentService
{
    private static final Logger log = LoggerFactory.getLogger(BbsCommentServiceImpl.class);

    @Autowired
    private BbsCommentMapper bbsCommentMapper;

    @Autowired
    private BbsLikeMapper bbsLikeMapper;

    @Autowired
    private BbsDislikeMapper bbsDislikeMapper;

    @Autowired
    private BbsPostMapper bbsPostMapper;

    @Autowired
    private IBbsNotificationService bbsNotificationService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Override
    public List<BbsComment> selectBbsCommentList(BbsComment bbsComment)
    {
        List<BbsComment> bbsComments = bbsCommentMapper.selectBbsCommentList(bbsComment);

        if (bbsComments != null && !bbsComments.isEmpty())
        {
            // 提取commentId集合
            List<Long> commentIdList = bbsComments.stream()
                    .map(BbsComment::getCommentId)
                    .collect(Collectors.toList());

            // 根据commentIdList集合，从点赞表bbs_like查询对应点赞集合，然后转成Map，方便对bbsComments匹配更新likeCount字段
            List<BbsLike> likeList = bbsLikeMapper.selectBbsLikeListByPostIds(commentIdList, "2");
            Map<Long, Long> likeCountMap = likeList.stream()
                    .collect(Collectors.groupingBy(BbsLike::getTargetId, Collectors.counting()));

            // 根据commentIdList集合，从点踩表bbs_dislike查询对应点踩集合，然后转成Map，方便对bbsComments匹配更新dislikeCount字段
            List<BbsDislike> dislikeList = bbsDislikeMapper.selectBbsDislikeListByTargetIds(commentIdList, "2");
            Map<Long, Long> dislikeCountMap = dislikeList.stream()
                    .collect(Collectors.groupingBy(BbsDislike::getTargetId, Collectors.counting()));

            // 获取当前登录用户信息，判断用户是否针对当前评论点赞、点踩
            String currentUserId = null;
            try {
                SysUser currentUser = SecurityUtils.getLoginUser().getUser();
                if (currentUser != null) {
                    currentUserId = currentUser.getUserName();
                }
            } catch (Exception e) {
                // 未登录用户，忽略
            }

            // 当前用户点赞过的评论ID集合
            Set<Long> likedCommentIds = null;
            if (currentUserId != null) {
                final String userId = currentUserId;
                likedCommentIds = likeList.stream()
                        .filter(like -> userId.equals(like.getUserId()))
                        .map(BbsLike::getTargetId)
                        .collect(Collectors.toSet());
            }

            // 当前用户点踩过的评论ID集合
            Set<Long> dislikedCommentIds = null;
            if (currentUserId != null) {
                final String userId = currentUserId;
                dislikedCommentIds = dislikeList.stream()
                        .filter(dislike -> userId.equals(dislike.getUserId()))
                        .map(BbsDislike::getTargetId)
                        .collect(Collectors.toSet());
            }

            for (BbsComment comment : bbsComments) {
                // 更新点赞数
                comment.setLikeCount(likeCountMap.getOrDefault(comment.getCommentId(), 0L).intValue());
                // 更新点踩数
                comment.setDislikeCount(dislikeCountMap.getOrDefault(comment.getCommentId(), 0L).intValue());
                // 设置当前用户是否已点赞
                comment.setIsLiked(likedCommentIds != null && likedCommentIds.contains(comment.getCommentId()));
                // 设置当前用户是否已点踩
                comment.setIsDisliked(dislikedCommentIds != null && dislikedCommentIds.contains(comment.getCommentId()));
            }
        }

        return bbsComments;
    }

    @Override
    public List<BbsComment> listCommentAnonymousByKey(String hashCode) {
        return  bbsCommentMapper.selectBbsCommentByHashCode(hashCode);
    }

    @Override
    public BbsComment selectBbsCommentById(Long commentId)
    {
        return bbsCommentMapper.selectBbsCommentById(commentId);
    }

    @Override
    @Transactional
    public int insertBbsComment(BbsComment bbsComment)
    {
        int rows = bbsCommentMapper.insertBbsComment(bbsComment);
        // 增加帖子评论数
        if (bbsComment.getPostId() != null)
        {
            bbsPostMapper.incrementCommentCount(bbsComment.getPostId());
            
            // 获取帖子信息，用于创建通知
            BbsPost post = bbsPostMapper.selectBbsPostById(bbsComment.getPostId());
            if (post != null)
            {
                // 判断是评论还是回复
                if (bbsComment.getParentId() != null && bbsComment.getParentId() > 0)
                {
                    // 这是回复，需要通知被回复的用户
                    if (bbsComment.getReplyUserId() != null)
                    {
                        bbsNotificationService.createReplyNotification(
                            bbsComment.getPostId(), 
                            bbsComment.getCommentId(), 
                            bbsComment.getUserId(),
                            bbsComment.getIsAnonymous(),
                            bbsComment.getReplyUserId()
                        );
                    }
                }
                else
                {
                    // 这是一级评论，需要通知帖子作者
                    bbsNotificationService.createCommentNotification(
                        bbsComment.getPostId(), 
                        bbsComment.getCommentId(), 
                        bbsComment.getUserId(),
                        bbsComment.getIsAnonymous(),
                        post.getUserId()
                    );
                }

            }
        }
        return rows;
    }

    @Override
    public int updateBbsComment(BbsComment bbsComment)
    {
        return bbsCommentMapper.updateBbsComment(bbsComment);
    }

    @Override
    @Transactional
    public int deleteBbsCommentByIds(Long[] commentIds)
    {
        return bbsCommentMapper.deleteBbsCommentByIds(commentIds);
    }

    @Override
    @Transactional
    public int toggleLike(Long commentId, String userId)
    {
        BbsLike bbsLike = new BbsLike();
        bbsLike.setTargetType("2");
        bbsLike.setTargetId(commentId);
        bbsLike.setUserId(userId);
        
        BbsLike existLike = bbsLikeMapper.selectBbsLike(bbsLike);
        if (existLike != null)
        {
            bbsLikeMapper.deleteBbsLike(bbsLike);
            bbsCommentMapper.decrementLikeCount(commentId);
            return -1;
        }
        else
        {
            bbsLikeMapper.insertBbsLike(bbsLike);
            bbsCommentMapper.incrementLikeCount(commentId);
            return 1;
        }
    }

    @Override
    @Transactional
    public int delByPersonal(Long commentId, String anonymousUserHash) {
        int rows = 0;
        SysUser user = SecurityUtils.getLoginUser().getUser();

        BbsComment bbsComment = bbsCommentMapper.selectBbsCommentById(commentId);
        if(null == bbsComment){
            throw new RuntimeException("评论不存在");
        }

        String commentUserId = bbsComment.getUserId();
        String currentUserId = user.getUserId() == null ? "" : String.valueOf(user.getUserId());
        String currentUserName = user.getUserName();
        boolean ownerMatched;
        if ("1".equals(bbsComment.getIsAnonymous())) {
            ownerMatched = commentUserId != null && commentUserId.equals(anonymousUserHash);
        } else {
            ownerMatched = commentUserId != null
                && (commentUserId.equals(currentUserId) || commentUserId.equals(currentUserName)
                    || (bbsComment.getCreateBy() != null && bbsComment.getCreateBy().equals(currentUserName)));
        }

        if (ownerMatched) {
            Long[] commentIds = { Long.valueOf(commentId) };
             bbsCommentMapper.deleteBbsCommentByParentCommentIds(commentIds);

            rows = bbsCommentMapper.deleteBbsCommentById(commentId);
        }else{
            throw new RuntimeException("只能删除自己的评论");
        }

        return rows;
    }

    @Override
    @Transactional
    public int delByAdmin(Long[] commentIds) {
        SysUser user = SecurityUtils.getLoginUser().getUser();

        String adminConfig = sysConfigService.selectConfigByKey("sys.account.admin");
        if (SecurityUtils.isConfigAdmin(adminConfig, user)) {
            bbsCommentMapper.deleteBbsCommentByParentCommentIds(commentIds);
            return bbsCommentMapper.deleteBbsCommentByIds(commentIds);
        }else{
            throw new RuntimeException("您没有权限删除评论");
        }
    }
}
