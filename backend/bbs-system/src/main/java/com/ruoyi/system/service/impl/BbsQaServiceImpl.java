package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.BbsPointRecord;
import com.ruoyi.system.domain.BbsQaAnswer;
import com.ruoyi.system.domain.BbsQaQuestion;
import com.ruoyi.system.mapper.BbsPointRecordMapper;
import com.ruoyi.system.mapper.BbsQaMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsQaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BbsQaServiceImpl implements IBbsQaService
{
    @Autowired
    private BbsQaMapper bbsQaMapper;

    @Autowired
    private BbsPointRecordMapper bbsPointRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public Long createQuestion(BbsQaQuestion question, String userId, String nickName, String avatar, String deptName)
    {
        if (question.getRewardPoints() == null || question.getRewardPoints() < 0)
        {
            question.setRewardPoints(0);
        }
        int reward = question.getRewardPoints();

        question.setAskUserId(userId);
        question.setAskNickName(nickName);
        question.setAskAvatar(avatar);
        question.setAskDeptName(deptName);
        question.setStatus("open");
        question.setFrozenPoints(reward);
        question.setViewCount(0);
        question.setAnswerCount(0);
        if (question.getAuditStatus() == null)
        {
            question.setAuditStatus("approved"); // 默认无需审核，可配置调整
        }
        if (question.getVisibilityScope() == null)
        {
            question.setVisibilityScope("all");
        }
        if (question.getCategory() == null)
        {
            question.setCategory("other");
        }
        question.setCreateBy(userId);
        bbsQaMapper.insertQuestion(question);

        // 冻结积分：先保存问题以取得问题编号，再从提问者账户扣除；积分不足时抛出异常，整个事务回滚
        if (reward > 0)
        {
            int affected = sysUserMapper.decrementBbsPoints(userId, reward);
            if (affected == 0)
            {
                throw new ServiceException("积分不足，无法发起悬赏提问");
            }
            BbsPointRecord freezeRecord = new BbsPointRecord();
            freezeRecord.setUserId(userId);
            freezeRecord.setActionType("QA_FREEZE");
            freezeRecord.setBizType("qa_question");
            freezeRecord.setBizId(question.getQuestionId());
            freezeRecord.setPointChange(-reward);
            freezeRecord.setRemark("悬赏提问冻结积分");
            freezeRecord.setDelFlag("0");
            bbsPointRecordMapper.insertIgnore(freezeRecord);
        }
        return question.getQuestionId();
    }

    @Override
    public List<BbsQaQuestion> listQuestions(String status, String category, String keyword, String orderBy)
    {
        return bbsQaMapper.selectQuestionList(status, category, keyword, orderBy);
    }

    @Override
    public List<BbsQaQuestion> listArchivedQuestions(String category, String keyword, String orderBy)
    {
        return bbsQaMapper.selectArchivedList(category, keyword, orderBy);
    }

    @Override
    @Transactional
    public BbsQaQuestion getQuestionDetail(Long questionId, String currentUserId)
    {
        BbsQaQuestion question = bbsQaMapper.selectQuestionById(questionId);
        if (question == null)
        {
            return null;
        }
        // 异步增加浏览数
        bbsQaMapper.incrementViewCount(questionId);
        question.setViewCount(question.getViewCount() == null ? 1 : question.getViewCount() + 1);

        List<BbsQaAnswer> answers = bbsQaMapper.selectAnswerListByQuestionId(questionId);
        question.setAnswers(answers);

        if (currentUserId != null)
        {
            question.setIsCurrentUserAsker(currentUserId.equals(question.getAskUserId()));
        }
        return question;
    }

    @Override
    @Transactional
    public Long answerQuestion(Long questionId, String content, Long parentAnswerId,
                               String userId, String nickName, String avatar, String deptName)
    {
        BbsQaQuestion question = bbsQaMapper.selectQuestionById(questionId);
        if (question == null)
        {
            throw new ServiceException("问题不存在");
        }
        if (!"open".equals(question.getStatus()))
        {
            throw new ServiceException("该问题已关闭，不允许继续回答");
        }
        if (userId.equals(question.getAskUserId()))
        {
            throw new ServiceException("不允许对自己的问题回答");
        }

        BbsQaAnswer answer = new BbsQaAnswer();
        answer.setQuestionId(questionId);
        answer.setParentAnswerId(parentAnswerId);
        answer.setContent(content);
        answer.setIsBest("0");
        answer.setLikeCount(0);
        answer.setAnswerUserId(userId);
        answer.setAnswerNickName(nickName);
        answer.setAnswerAvatar(avatar);
        answer.setAnswerDeptName(deptName);
        answer.setCreateBy(userId);
        bbsQaMapper.insertAnswer(answer);
        bbsQaMapper.incrementAnswerCount(questionId);
        return answer.getAnswerId();
    }

    @Override
    @Transactional
    public void acceptBestAnswer(Long questionId, Long answerId, String currentUserId)
    {
        BbsQaQuestion question = bbsQaMapper.selectQuestionById(questionId);
        if (question == null)
        {
            throw new ServiceException("问题不存在");
        }
        if (!currentUserId.equals(question.getAskUserId()))
        {
            throw new ServiceException("只有提问者可以采纳最佳答案");
        }
        if (!"open".equals(question.getStatus()))
        {
            throw new ServiceException("该问题已被采纳或已关闭");
        }
        BbsQaAnswer answer = bbsQaMapper.selectAnswerById(answerId);
        if (answer == null || !questionId.equals(answer.getQuestionId()))
        {
            throw new ServiceException("回答不存在或不属于该问题");
        }

        bbsQaMapper.clearBestAnswer(questionId);
        bbsQaMapper.markBestAnswer(answerId);
        bbsQaMapper.updateQuestionSolved(questionId, answerId);

        // 发放悬赏积分给最佳回答者
        Integer points = question.getFrozenPoints() == null ? question.getRewardPoints() : question.getFrozenPoints();
        if (points == null) points = 0;
        if (points > 0)
        {
            BbsPointRecord record = new BbsPointRecord();
            record.setUserId(answer.getAnswerUserId());
            record.setActionType("QA_BEST_ANSWER");
            record.setBizType("qa_question");
            record.setBizId(questionId);
            record.setPointChange(points);
            record.setRemark("最佳答案悬赏");
            record.setDelFlag("0");
            bbsPointRecordMapper.insertIgnore(record);
            sysUserMapper.incrementBbsPoints(answer.getAnswerUserId(), points);
        }
    }

    @Override
    @Transactional
    public void cancelQuestion(Long questionId, String currentUserId)
    {
        BbsQaQuestion question = bbsQaMapper.selectQuestionById(questionId);
        if (question == null)
        {
            throw new ServiceException("问题不存在");
        }
        if (!currentUserId.equals(question.getAskUserId()))
        {
            throw new ServiceException("只有提问者可以撤销问题");
        }
        if (!"open".equals(question.getStatus()))
        {
            throw new ServiceException("该问题已关闭，无法撤销");
        }

        bbsQaMapper.updateQuestionCancelled(questionId);

        // 退回冻结积分
        Integer frozenPoints = question.getFrozenPoints();
        if (frozenPoints != null && frozenPoints > 0)
        {
            sysUserMapper.incrementBbsPoints(currentUserId, frozenPoints);
            BbsPointRecord record = new BbsPointRecord();
            record.setUserId(currentUserId);
            record.setActionType("QA_REFUND");
            record.setBizType("qa_question");
            record.setBizId(questionId);
            record.setPointChange(frozenPoints);
            record.setRemark("撤销问题退回积分");
            record.setDelFlag("0");
            bbsPointRecordMapper.insertIgnore(record);
        }
    }

    @Override
    public void archiveQuestion(Long questionId, String currentUserId)
    {
        BbsQaQuestion question = bbsQaMapper.selectQuestionById(questionId);
        if (question == null)
        {
            throw new ServiceException("问题不存在");
        }
        if (!currentUserId.equals(question.getAskUserId()))
        {
            throw new ServiceException("只有提问者可以归档问题");
        }
        if (!"solved".equals(question.getStatus()))
        {
            throw new ServiceException("只有已解决的问题可以归档");
        }
        bbsQaMapper.updateQuestionArchived(questionId);
    }

    @Override
    @Transactional
    public boolean toggleLikeAnswer(Long answerId, String userId)
    {
        BbsQaAnswer answer = bbsQaMapper.selectAnswerById(answerId);
        if (answer == null)
        {
            throw new ServiceException("回答不存在");
        }
        // 简化实现：未维护点赞希希表，直接增加点赞
        // 如需防重复点赞可引入 bbs_qa_answer_like 表
        bbsQaMapper.incrementAnswerLike(answerId);
        return true;
    }

    @Override
    @Transactional
    public void adminAcceptBestAnswer(Long questionId, Long answerId)
    {
        BbsQaQuestion question = bbsQaMapper.selectQuestionById(questionId);
        if (question == null)
        {
            throw new ServiceException("问题不存在");
        }
        BbsQaAnswer answer = bbsQaMapper.selectAnswerById(answerId);
        if (answer == null || !questionId.equals(answer.getQuestionId()))
        {
            throw new ServiceException("回答不存在或不属于该问题");
        }

        bbsQaMapper.clearBestAnswer(questionId);
        bbsQaMapper.markBestAnswer(answerId);
        bbsQaMapper.updateQuestionSolved(questionId, answerId);

        Integer points = question.getFrozenPoints() == null ? question.getRewardPoints() : question.getFrozenPoints();
        if (points == null) points = 0;
        if (points > 0)
        {
            BbsPointRecord record = new BbsPointRecord();
            record.setUserId(answer.getAnswerUserId());
            record.setActionType("QA_BEST_ANSWER_ADMIN");
            record.setBizType("qa_question");
            record.setBizId(questionId);
            record.setPointChange(points);
            record.setRemark("管理员代为采纳最佳答案悬赏");
            record.setDelFlag("0");
            bbsPointRecordMapper.insertIgnore(record);
            sysUserMapper.incrementBbsPoints(answer.getAnswerUserId(), points);
        }
    }

    @Override
    public void auditQuestion(Long questionId, String auditStatus, String auditReason)
    {
        bbsQaMapper.updateQuestionAudit(questionId, auditStatus, auditReason);
    }
}