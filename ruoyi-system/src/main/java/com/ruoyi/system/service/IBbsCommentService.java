package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BbsComment;

public interface IBbsCommentService
{
    public List<BbsComment> selectBbsCommentList(BbsComment bbsComment);
    public List<BbsComment> listCommentAnonymousByKey(String hashCode);
    public BbsComment selectBbsCommentById(Long commentId);
    public int insertBbsComment(BbsComment bbsComment);
    public int updateBbsComment(BbsComment bbsComment);
    public int deleteBbsCommentByIds(Long[] commentIds);
    public int toggleLike(Long commentId, String userId);

    /**
     * 个人删除自己的评论,物理删除
     * @param commentId
     * @return
     */
    public int delByPersonal(Long commentId);

    /**
     * 管理员删除评论,支持批量删除,虚拟删除,只变更状态
     * @param commentIds
     * @return
     */
    public int delByAdmin(Long[] commentIds);
}
