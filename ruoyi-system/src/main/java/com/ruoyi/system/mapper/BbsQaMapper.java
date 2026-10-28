package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsQaAnswer;
import com.ruoyi.system.domain.BbsQaQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BbsQaMapper
{
    List<BbsQaQuestion> selectQuestionList(@Param("status") String status,
                                            @Param("category") String category,
                                            @Param("keyword") String keyword,
                                            @Param("orderBy") String orderBy);

    List<BbsQaQuestion> selectArchivedList(@Param("category") String category,
                                            @Param("keyword") String keyword,
                                            @Param("orderBy") String orderBy);

    BbsQaQuestion selectQuestionById(@Param("questionId") Long questionId);

    List<BbsQaAnswer> selectAnswerListByQuestionId(@Param("questionId") Long questionId);

    int insertQuestion(BbsQaQuestion question);

    int insertAnswer(BbsQaAnswer answer);

    int clearBestAnswer(@Param("questionId") Long questionId);

    int markBestAnswer(@Param("answerId") Long answerId);

    int updateQuestionSolved(@Param("questionId") Long questionId,
                              @Param("bestAnswerId") Long bestAnswerId);

    int updateQuestionArchived(@Param("questionId") Long questionId);

    int updateQuestionCancelled(@Param("questionId") Long questionId);

    BbsQaAnswer selectAnswerById(@Param("answerId") Long answerId);

    int incrementViewCount(@Param("questionId") Long questionId);

    int incrementAnswerCount(@Param("questionId") Long questionId);

    int incrementAnswerLike(@Param("answerId") Long answerId);

    int decrementAnswerLike(@Param("answerId") Long answerId);

    int updateQuestionAudit(@Param("questionId") Long questionId,
                             @Param("auditStatus") String auditStatus,
                             @Param("auditReason") String auditReason);

    int deleteAnswerById(@Param("answerId") Long answerId);

    int countMyQuestions(@Param("askUserId") String askUserId);

    int countMyAnswers(@Param("answerUserId") String answerUserId);
}
