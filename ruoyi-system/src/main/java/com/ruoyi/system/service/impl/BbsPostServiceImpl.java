package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BbsComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.system.domain.BbsLike;
import com.ruoyi.system.domain.BbsCollect;
import com.ruoyi.system.domain.BbsPost;
import com.ruoyi.system.mapper.BbsLikeMapper;
import com.ruoyi.system.mapper.BbsCollectMapper;
import com.ruoyi.system.mapper.BbsPostMapper;
import com.ruoyi.system.mapper.BbsCommentMapper;
import com.ruoyi.system.mapper.BbsCategoryMapper;
import com.ruoyi.system.service.IBbsPostService;
import com.ruoyi.system.service.IBbsNotificationService;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.text.Convert;

/**
 * 帖子 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class BbsPostServiceImpl implements IBbsPostService
{
    @Autowired
    private BbsPostMapper bbsPostMapper;

    @Autowired
    private BbsLikeMapper bbsLikeMapper;

    @Autowired
    private BbsCollectMapper bbsCollectMapper;

    @Autowired
    private BbsCategoryMapper bbsCategoryMapper;

    @Autowired
    private BbsCommentMapper bbsCommentMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private IBbsNotificationService bbsNotificationService;

    /**
     * 查询帖子信息
     * 
     * @param postId 帖子ID
     * @return 帖子信息
     */
    @Override
    public BbsPost selectBbsPostById(Long postId)
    {
        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post != null)
        {
            // 如果用户未登录，设置为false
            post.setIsLiked(false);
            post.setIsCollected(false);
        }
        return post;
    }

    /**
     * 查询帖子信息（包含用户点赞/收藏状态）
     * 
     * @param postId 帖子ID
     * @param userId 用户ID（可为空，如果为空则不查询用户状态）
     * @return 帖子信息
     */
    @Override
    public BbsPost selectBbsPostById(Long postId, String userId)
    {
        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post != null && StringUtils.isNotEmpty(userId))
        {
            // 查询用户是否已点赞
            BbsLike bbsLike = new BbsLike();
            bbsLike.setTargetType("1"); // 1表示帖子
            bbsLike.setTargetId(postId);
            bbsLike.setUserId(userId);
            BbsLike existLike = bbsLikeMapper.selectBbsLike(bbsLike);
            post.setIsLiked(existLike != null);

            // 查询用户是否已收藏
            BbsCollect bbsCollect = new BbsCollect();
            bbsCollect.setPostId(postId);
            bbsCollect.setUserId(userId);
            BbsCollect existCollect = bbsCollectMapper.selectBbsCollect(bbsCollect);
            post.setIsCollected(existCollect != null);
        }
        else if (post != null)
        {
            // 如果用户未登录，设置为false
            post.setIsLiked(false);
            post.setIsCollected(false);
        }
        return post;
    }

    @Value("${isAdmin}")
    private String isAdmin;

    /**
     * 查询帖子列表
     * 
     * @param bbsPost 帖子信息
     * @return 帖子集合
     */
    @Override
    public List<BbsPost> selectBbsPostList(BbsPost bbsPost)
    {
        List<BbsPost> bbsPosts = bbsPostMapper.selectBbsPostList(bbsPost);

        if(bbsPosts != null && !bbsPosts.isEmpty()){
            List<Long> postIdList = bbsPosts.stream().map(BbsPost::getPostId).collect(Collectors.toList());

            // 根据postIdList集合，从收藏表bbs_collect查询对应收藏集合，然后转成Map
            List<BbsCollect> collectList = bbsCollectMapper.selectBbsCollectListByPostIds(postIdList);
            Map<Long, Long> collectCountMap = collectList.stream()
                    .collect(Collectors.groupingBy(BbsCollect::getPostId, Collectors.counting()));

            // 根据postIdList集合，从评论表bbs_comment查询对应评论集合，然后转成Map
            List<BbsComment> commentList = bbsCommentMapper.selectBbsCommentListByPostIds(postIdList);
            Map<Long, Long> commentCountMap = commentList.stream()
                    .collect(Collectors.groupingBy(BbsComment::getPostId, Collectors.counting()));

            // 根据postIdList集合，从点赞表bbs_like查询对应点赞集合，然后转成Map
            List<BbsLike> likeList = bbsLikeMapper.selectBbsLikeListByPostIds(postIdList, "1");
            Map<Long, Long> likeCountMap = likeList.stream()
                    .collect(Collectors.groupingBy(BbsLike::getTargetId, Collectors.counting()));

            for (BbsPost post : bbsPosts) {
                // 更新收藏、评论、点赞数
                post.setCollectCount(collectCountMap.getOrDefault(post.getPostId(), 0L).intValue());
                post.setCommentCount(commentCountMap.getOrDefault(post.getPostId(), 0L).intValue());
                post.setLikeCount(likeCountMap.getOrDefault(post.getPostId(), 0L).intValue());

                String createBy = post.getCreateBy();
                if(isAdmin.contains(createBy)){
                    post.setCreateBy("admin");
                }else {
                    post.setCreateBy("");
                }
            }
        }
        return bbsPosts;
    }

    @Override
    public List<BbsPost> listAnonymousByKey(String hashCode) {
        List<BbsPost> bbsPostList = bbsPostMapper.selectBbsPostListByHashCode(hashCode);
        return bbsPostList;
    }

    /**
     * 新增帖子
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    @Override
    @Transactional
    public Long insertBbsPost(BbsPost bbsPost)
    {
        // 如果是草稿（status="4"），直接保存，不进行其他状态设置
        if (!"4".equals(bbsPost.getStatus()))
        {
            // 检查审核开关，如果开启审核，则设置状态为待审核(2)，否则为正常(0)
            String auditEnabled = sysConfigService.selectConfigByKey("bbs.post.audit.enabled");
            if (StringUtils.isEmpty(auditEnabled))
            {
                // 如果配置不存在，默认关闭审核，状态为正常
                auditEnabled = "false";
            }
            
            // 如果审核开关开启，且当前状态未设置，则设置为待审核
            if (Convert.toBool(auditEnabled) && bbsPost.getStatus() == null)
            {
                bbsPost.setStatus("2"); // 待审核
            }
            else if (bbsPost.getStatus() == null)
            {
                bbsPost.setStatus("0"); // 正常
            }
        }

        int rows = bbsPostMapper.insertBbsPost(bbsPost);
        // 增加分类帖子数（只有审核通过或直接发布的才增加，草稿不增加）
        if (bbsPost.getCategoryId() != null && "0".equals(bbsPost.getStatus()))
        {
            bbsCategoryMapper.incrementPostCount(bbsPost.getCategoryId());
        }
        return bbsPost.getPostId();
    }

    /**
     * 修改帖子
     * 
     * @param bbsPost 帖子信息
     * @return 结果
     */
    @Override
    public int updateBbsPost(BbsPost bbsPost)
    {
        return bbsPostMapper.updateBbsPost(bbsPost);
    }

    /**
     * 批量删除帖子对象
     * 
     * @param postIds 需要删除的帖子ID
     * @return 结果
     */
    @Override
    public int deleteBbsPostByIds(Long[] postIds)
    {
        for (Long postId : postIds) {
            BbsPost bbsPost = bbsPostMapper.selectBbsPostById(postId);
            if(bbsPost != null && bbsPost.getCategoryId()!=null){
                bbsCategoryMapper.decrementPostCount(bbsPost.getCategoryId());
            }
        }
        return bbsPostMapper.deleteBbsPostByIds(postIds);
    }

    /**
     * 删除帖子信息
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    @Override
    public int deleteBbsPostById(Long postId)
    {

        BbsPost bbsPost = bbsPostMapper.selectBbsPostById(postId);
        if(bbsPost != null && bbsPost.getCategoryId()!=null){
            bbsCategoryMapper.decrementPostCount(bbsPost.getCategoryId());
        }
        return bbsPostMapper.deleteBbsPostById(postId);
    }

    /**
     * 真正删除草稿（设置del_flag='2'）
     * 
     * @param postIds 需要删除的帖子ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int realDeleteBbsPostByIds(Long[] postIds)
    {
        // 减少分类帖子数
        for (Long postId : postIds) {
            BbsPost bbsPost = bbsPostMapper.selectBbsPostById(postId);
            if(bbsPost != null && bbsPost.getCategoryId() != null){
                bbsCategoryMapper.decrementPostCount(bbsPost.getCategoryId());
            }
        }
        return bbsPostMapper.realDeleteBbsPostByIds(postIds);
    }

    /**
     * 增加浏览数
     * 
     * @param postId 帖子ID
     * @return 结果
     */
    @Override
    public int incrementViewCount(Long postId)
    {
        return bbsPostMapper.incrementViewCount(postId);
    }

    /**
     * 点赞/取消点赞
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 结果（1点赞成功，-1取消点赞）
     */
    @Override
    @Transactional
    public int toggleLike(Long postId, String userId)
    {
        BbsLike bbsLike = new BbsLike();
        bbsLike.setTargetType("1");
        bbsLike.setTargetId(postId);
        bbsLike.setUserId(userId);
        
        BbsLike existLike = bbsLikeMapper.selectBbsLike(bbsLike);
        if (existLike != null)
        {
            // 取消点赞
            bbsLikeMapper.deleteBbsLike(bbsLike);
            bbsPostMapper.decrementLikeCount(postId);
            return -1;
        }
        else
        {
            // 点赞
            bbsLikeMapper.insertBbsLike(bbsLike);
            bbsPostMapper.incrementLikeCount(postId);
            
            // 创建点赞通知
            BbsPost post = bbsPostMapper.selectBbsPostById(postId);
            if (post != null)
            {
                bbsNotificationService.createLikeNotification(postId, userId, post.getUserId());
            }
            
            return 1;
        }
    }

    /**
     * 收藏/取消收藏
     * 
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 结果（1收藏成功，-1取消收藏）
     */
    @Override
    @Transactional
    public int toggleCollect(Long postId, String userId)
    {
        BbsCollect bbsCollect = new BbsCollect();
        bbsCollect.setPostId(postId);
        bbsCollect.setUserId(userId);
        
        BbsCollect existCollect = bbsCollectMapper.selectBbsCollect(bbsCollect);
        if (existCollect != null)
        {
            // 取消收藏
            bbsCollectMapper.deleteBbsCollect(bbsCollect);
            bbsPostMapper.decrementCollectCount(postId);
            return -1;
        }
        else
        {
            // 收藏
            bbsCollectMapper.insertBbsCollect(bbsCollect);
            bbsPostMapper.incrementCollectCount(postId);
            
            // 创建收藏通知
            BbsPost post = bbsPostMapper.selectBbsPostById(postId);
            if (post != null)
            {
                bbsNotificationService.createCollectNotification(postId, userId, post.getUserId());
            }
            
            return 1;
        }
    }

    /**
     * 查询热门文章列表
     * 
     * @param limit 限制数量
     * @return 帖子集合
     */
    @Override
    public List<BbsPost> selectHotPostList(Integer limit)
    {
        if (limit == null || limit <= 0)
        {
            limit = 5;
        }
        return bbsPostMapper.selectHotPostList(limit);
    }

    /**
     * 批量审核通过文章
     * 
     * @param postIds 需要审核通过的帖子ID数组
     * @return 结果
     */
    @Override
    @Transactional
    public int approvePosts(Long[] postIds)
    {
        int successCount = 0;
        for (Long postId : postIds)
        {
            BbsPost post = bbsPostMapper.selectBbsPostById(postId);
            if (post != null && "2".equals(post.getStatus()))
            {
                // 只有待审核状态的文章才能审核通过
                post.setStatus("0");
                post.setAuditReason(null); // 清除审核不通过原因
                bbsPostMapper.updateBbsPost(post);
                // 如果之前是待审核状态，现在审核通过，需要增加分类帖子数
                if (post.getCategoryId() != null)
                {
                    bbsCategoryMapper.incrementPostCount(post.getCategoryId());
                }
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 批量审核不通过文章
     * 
     * @param postIds 需要审核不通过的帖子ID数组
     * @param auditReason 审核不通过原因
     * @return 结果
     */
    @Override
    @Transactional
    public int rejectPosts(Long[] postIds, String auditReason)
    {
        int successCount = 0;
        for (Long postId : postIds)
        {
            BbsPost post = bbsPostMapper.selectBbsPostById(postId);
            if (post != null && "2".equals(post.getStatus()))
            {
                // 只有待审核状态的文章才能审核不通过
                post.setStatus("3"); // 审核不通过
                post.setAuditReason(null); // 不填写审核不通过原因
                bbsPostMapper.updateBbsPost(post);
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 置顶/取消置顶
     * 
     * @param postId 帖子ID
     * @return 结果（1置顶成功，-1取消置顶）
     */
    @Override
    public int toggleTop(Long postId)
    {
        BbsPost post = bbsPostMapper.selectBbsPostById(postId);
        if (post == null)
        {
            return 0;
        }
        
        if ("1".equals(post.getIsTop()))
        {
            // 取消置顶
            post.setIsTop("0");
            bbsPostMapper.updateBbsPost(post);
            return -1;
        }
        else
        {
            // 置顶
            post.setIsTop("1");
            bbsPostMapper.updateBbsPost(post);
            return 1;
        }
    }

    /**
     * 下架/上架帖子
     * 
     * @param postIds 帖子ID数组
     * @return 结果（成功数量）
     */
    @Override
    @Transactional
    public int toggleOffline(Long[] postIds)
    {
        int successCount = 0;
        for (Long postId : postIds)
        {
            BbsPost post = bbsPostMapper.selectBbsPostById(postId);
            if (post != null && post.getDelFlag() != null && "0".equals(post.getDelFlag()))
            {
                String originalStatus = post.getStatus();
                if ("1".equals(originalStatus))
                {
                    // 当前是下架状态，执行上架操作
                    post.setStatus("0"); // 设置为正常状态
                    bbsPostMapper.updateBbsPost(post);
                    // 上架后需要增加分类帖子数
                    if (post.getCategoryId() != null)
                    {
                        bbsCategoryMapper.incrementPostCount(post.getCategoryId());
                    }
                    successCount++;
                }
                else if ("0".equals(originalStatus) || "2".equals(originalStatus) || "3".equals(originalStatus))
                {
                    // 当前是正常、待审核或审核不通过状态，执行下架操作
                    // 下架前先减少分类帖子数（只有正常状态的才减少）
                    if (post.getCategoryId() != null && "0".equals(originalStatus))
                    {
                        bbsCategoryMapper.decrementPostCount(post.getCategoryId());
                    }
                    post.setStatus("1"); // 设置为已关闭状态
                    bbsPostMapper.updateBbsPost(post);
                    successCount++;
                }
            }
        }
        return successCount;
    }

    @Override
    public int delByPersonal(Long postId) {
        SysUser user = SecurityUtils.getLoginUser().getUser();

        BbsPost bbsPost = bbsPostMapper.selectBbsPostById(postId);
        if(null == bbsPost){
            throw new ServiceException("帖子不存在");
        }

        // 越权判断
        if(!user.getUserId().equals(bbsPost.getUserId())){
            throw new ServiceException("只能删除自己的帖子");
        }

        return bbsPostMapper.deleteBbsPostById(postId);
    }
}
