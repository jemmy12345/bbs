package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.BbsComment;
import com.ruoyi.system.domain.bo.CommentManageBo;
import com.ruoyi.system.domain.bo.CommentManageQueryBo;
import com.ruoyi.system.domain.vo.CommentManageVo;
import com.ruoyi.system.mapper.BbsCommentMapper;
import com.ruoyi.system.service.IBbsCommentManageService;
import com.ruoyi.system.service.IBbsPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BbsCommentManageServiceImpl implements IBbsCommentManageService
{
    @Autowired
    private IBbsPostService bbsPostService;
    @Autowired
    private BbsCommentMapper bbsCommentMapper;


    @Override
    public List<CommentManageVo> list(CommentManageQueryBo commentManageQueryBo) {

        List<CommentManageVo> commentList = bbsCommentMapper.selectBbsPostCommentList(commentManageQueryBo);

        return commentList;
    }

    @Override
    @Transactional
    public int setDelFlag(CommentManageBo commentManageBo) {
        Long commentId = commentManageBo.getCommentId();
        String commentDelFlag = commentManageBo.getCommentDelFlag();

        BbsComment bbsComment = bbsCommentMapper.selectBbsCommentForAdminById(commentId);
        if(null == bbsComment){
            throw new ServiceException("评论不存在");
        }

        bbsComment.setDelFlag(commentDelFlag);
        bbsComment.setUpdateBy(SecurityUtils.getLoginUser().getUserId());

        return bbsCommentMapper.updateBbsComment(bbsComment);
    }

}
