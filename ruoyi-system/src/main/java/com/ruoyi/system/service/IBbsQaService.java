package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsQaAnswer;
import com.ruoyi.system.domain.BbsQaQuestion;

import java.util.List;

public interface IBbsQaService
{
    /** 创建问题（同时冻结悬赏积分） */
    Long createQuestion(BbsQaQuestion question, String userId, String nickName, String avatar, String deptName);

    /** 问题列表（支持多维筛选和排序） */
    List<BbsQaQuestion> listQuestions(String status, String category, String keyword, String orderBy);

    /** 已归档知识库列表 */
    List<BbsQaQuestion> listArchivedQuestions(String category, String keyword, String orderBy);

    /** 问题详情（包含回答列表，增加浏览数） */
    BbsQaQuestion getQuestionDetail(Long questionId, String currentUserId);

    /** 回答问题 */
    Long answerQuestion(Long questionId, String content, Long parentAnswerId,
                        String userId, String nickName, String avatar, String deptName);

    /** 提问者采纳最佳答案（发放悬赏积分） */
    void acceptBestAnswer(Long questionId, Long answerId, String currentUserId);

    /** 撤销问题（退回冻结积分） */
    void cancelQuestion(Long questionId, String currentUserId);

    /** 将已解决的问题归档到知识库 */
    void archiveQuestion(Long questionId, String currentUserId);

    /** 点赞/取消点赞回答 */
    boolean toggleLikeAnswer(Long answerId, String userId);

    /** 管理员强制采纳（逃期未采纳时） */
    void adminAcceptBestAnswer(Long questionId, Long answerId);

    /** 管理员审核问题 */
    void auditQuestion(Long questionId, String auditStatus, String auditReason);
}
