package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsComment;
import com.ruoyi.system.domain.bo.CommentManageQueryBo;
import com.ruoyi.system.domain.vo.CommentManageVo;

/**
 * 评论表 数据层
 * 
 * @author ruoyi
 */
public interface BbsCommentMapper
{
    /**
     * 查询评论信息
     * 
     * @param commentId 评论ID
     * @return 评论信息
     */
    public BbsComment selectBbsCommentById(Long commentId);


    public BbsComment selectBbsCommentForAdminById(Long commentId);

    /**
     * 查询评论列表
     * 
     * @param bbsComment 评论信息
     * @return 评论集合
     */
    public List<BbsComment> selectBbsCommentList(BbsComment bbsComment);

    /**
     *  查询个人匿名评论及回复
     * @param hashCode
     * @return
     */
    public List<BbsComment> selectBbsCommentByHashCode(String hashCode);

    /**
     * 新增评论
     * 
     * @param bbsComment 评论信息
     * @return 结果
     */
    public int insertBbsComment(BbsComment bbsComment);

    /**
     * 修改评论
     * 
     * @param bbsComment 评论信息
     * @return 结果
     */
    public int updateBbsComment(BbsComment bbsComment);

    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @return 结果
     */
    public int deleteBbsCommentById(Long commentId);

    /**
     * 批量删除评论
     * 
     * @param commentIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsCommentByIds(Long[] commentIds);

    /**
     * 增加点赞数
     * 
     * @param commentId 评论ID
     * @return 结果
     */
    public int incrementLikeCount(Long commentId);

    /**
     * 减少点赞数
     * 
     * @param commentId 评论ID
     * @return 结果
     */
    public int decrementLikeCount(Long commentId);

    /**
     * 统计用户收到的评论数（用户发布的帖子收到的评论）
     * 
     * @param userId 用户ID
     * @return 评论数
     */
    public int countUserReceivedComments(String userId);

    /**
     * 根据父评论ID删除评论
     * @param commentId
     * @return
     */
    public int  deleteBbsCommentByParentCommentIds(Long[] commentId);

  public List<CommentManageVo> selectBbsPostCommentList(CommentManageQueryBo commentManageQueryBo);

    /**
     * 根据帖子ID集合查询评论列表
     * 
     * @param postIds 帖子ID集合
     * @return 评论集合
     */
    public List<BbsComment> selectBbsCommentListByPostIds(List<Long> postIds);

}
