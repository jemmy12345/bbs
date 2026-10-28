package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsIdeaProgress;
import com.ruoyi.system.domain.BbsIdeaProposal;
import com.ruoyi.system.domain.BbsIdeaReview;
import com.ruoyi.system.domain.BbsNotification;
import com.ruoyi.system.domain.BbsPointRecord;
import com.ruoyi.system.mapper.BbsIdeaMapper;
import com.ruoyi.system.mapper.BbsNotificationMapper;
import com.ruoyi.system.mapper.BbsPointRecordMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsIdeaService;
import com.ruoyi.system.service.IBbsIdeaStageReviewerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class BbsIdeaServiceImpl implements IBbsIdeaService
{
    private static final String STAGE_L1 = "L1";
    private static final String STAGE_L2 = "L2";
    private static final String STAGE_L3 = "L3";
    private static final String STAGE_NONE = "NONE";

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PENDING_L1 = "PENDING_L1";
    private static final String STATUS_L1_REVIEWING = "L1_REVIEWING";
    private static final String STATUS_L1_PASSED = "L1_PASSED";
    private static final String STATUS_L2_REVIEWING = "L2_REVIEWING";
    private static final String STATUS_L2_PASSED = "L2_PASSED";
    private static final String STATUS_L3_REVIEWING = "L3_REVIEWING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_IMPLEMENTING = "IMPLEMENTING";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_LANDED = "LANDED";
    private static final String STATUS_PUBLICIZED = "PUBLICIZED";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String VISIBILITY_ALL = "all";
    private static final String VISIBILITY_REVIEW_GROUP = "review_group";
    private static final String VISIBILITY_ADMIN = "admin";

    @Autowired
    private BbsIdeaMapper bbsIdeaMapper;

    @Autowired
    private BbsNotificationMapper bbsNotificationMapper;

    @Autowired
    private BbsPointRecordMapper bbsPointRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private IBbsIdeaStageReviewerConfigService reviewerConfigService;

    @Override
    @Transactional
    public Long saveOrSubmitIdea(BbsIdeaProposal idea, String userId, String nickName, boolean submit)
    {
        if (idea == null)
        {
            throw new ServiceException("提案参数不能为空");
        }

        idea.setSubmitUserId(userId);
        idea.setSubmitNickName(nickName);
        normalizeIdeaDefaults(idea);

        if (!submit)
        {
            idea.setIsDraft("1");
            idea.setStatus(STATUS_DRAFT);
            idea.setReviewStage(STAGE_NONE);
            idea.setReviewDeadline(null);
            bbsIdeaMapper.insertIdea(idea);
            return idea.getIdeaId();
        }

        validateSubmitForm(idea);

        Integer todayCount = bbsIdeaMapper.countTodaySubmissions(userId);
        if (todayCount != null && todayCount >= 3)
        {
            throw new ServiceException("每日最多投稿 3 篇");
        }

        Integer duplicateCount = bbsIdeaMapper.countDuplicateToday(userId, idea.getTitle());
        if (duplicateCount != null && duplicateCount > 0)
        {
            throw new ServiceException("今日已存在同标题提案，请勿重复提交");
        }

        idea.setIsDraft("0");
        idea.setStatus(STATUS_PENDING_L1);
        idea.setReviewStage(STAGE_L1);
        idea.setReviewDeadline(addDays(7));

        bbsIdeaMapper.insertIdea(idea);

        BbsIdeaProgress progress = new BbsIdeaProgress();
        progress.setIdeaId(idea.getIdeaId());
        progress.setStatusEnum(STATUS_PENDING_L1);
        progress.setUpdaterUserId(userId);
        progress.setUpdaterNickName(nickName);
        progress.setProgressStage("投稿成功");
        progress.setProgressDesc("提案已提交，进入初审队列");
        progress.setProgressPercent(10);
        bbsIdeaMapper.insertProgress(progress);

        // 通知L1评审人
        notifyReviewers(STAGE_L1, idea.getIdeaId(), "【初审待办】" + idea.getTitle(), "您有新的提案待初审，请及时处理");

        return idea.getIdeaId();
    }

    @Override
    @Transactional
    public void updateDraft(BbsIdeaProposal idea, String currentUserId)
    {
        BbsIdeaProposal existing = bbsIdeaMapper.selectIdeaById(idea.getIdeaId());
        if (existing == null)
        {
            throw new ServiceException("提案不存在");
        }
        if (!currentUserId.equals(existing.getSubmitUserId()))
        {
            throw new ServiceException("无权修改他人提案");
        }
        if (!STATUS_DRAFT.equals(existing.getStatus()))
        {
            throw new ServiceException("仅草稿状态可编辑");
        }
        normalizeIdeaDefaults(idea);
        idea.setIsDraft("1");
        idea.setStatus(STATUS_DRAFT);
        bbsIdeaMapper.updateIdea(idea);
    }

    @Override
    public List<BbsIdeaProposal> listIdeas(String status, String category, String keyword, String isPublicized,
                                           String currentUserId, boolean isAdmin, boolean canReviewAll, Boolean mine)
    {
        int mineOnly = Boolean.TRUE.equals(mine) ? 1 : 0;
        return bbsIdeaMapper.selectIdeaList(status, category, keyword, isPublicized, currentUserId,
                isAdmin ? 1 : 0, canReviewAll ? 1 : 0, mineOnly);
    }

    @Override
    public List<BbsIdeaProposal> listPublicizedWall(String sortBy, String category, String keyword, String currentUserId)
    {
        return bbsIdeaMapper.selectPublicizedWall(sortBy, category, keyword, currentUserId);
    }

    @Override
    public BbsIdeaProposal getIdeaDetail(Long ideaId, String currentUserId, boolean isAdmin, boolean canReviewAll)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            return null;
        }
        if (!canAccessIdea(idea, currentUserId, isAdmin, canReviewAll))
        {
            throw new ServiceException("无权查看该提案详情");
        }

        List<BbsIdeaProgress> progressList = bbsIdeaMapper.selectProgressList(ideaId);
        idea.setProgressList(progressList);

        boolean canViewAllReviews = canReviewAll
                || (currentUserId != null && currentUserId.equals(idea.getSubmitUserId()));

        List<BbsIdeaReview> rawReviews = bbsIdeaMapper.selectReviewList(ideaId);
        if (canViewAllReviews)
        {
            idea.setReviews(rawReviews);
        }
        else
        {
            List<BbsIdeaReview> filtered = new ArrayList<>();
            for (BbsIdeaReview review : rawReviews)
            {
                if ("1".equals(review.getIsVisibleToAll())
                        || (currentUserId != null && currentUserId.equals(review.getReviewerUserId())))
                {
                    filtered.add(review);
                }
            }
            idea.setReviews(filtered);
        }

        return idea;
    }

    private boolean canAccessIdea(BbsIdeaProposal idea, String currentUserId, boolean isAdmin, boolean canReviewAll)
    {
        if (idea == null)
        {
            return false;
        }
        if (STATUS_DRAFT.equals(idea.getStatus()) || "1".equals(idea.getIsDraft()))
        {
            return StringUtils.isNotBlank(currentUserId) && currentUserId.equals(idea.getSubmitUserId());
        }
        if ("1".equals(idea.getIsPublicized()) || isAdmin)
        {
            return true;
        }
        if (StringUtils.isNotBlank(currentUserId) && currentUserId.equals(idea.getSubmitUserId()))
        {
            return true;
        }

        String visibilityScope = StringUtils.isBlank(idea.getVisibilityScope())
                ? VISIBILITY_REVIEW_GROUP
                : idea.getVisibilityScope();
        if (VISIBILITY_ALL.equals(visibilityScope))
        {
            return true;
        }
        if (VISIBILITY_ADMIN.equals(visibilityScope))
        {
            return false;
        }
        if (canReviewAll)
        {
            return true;
        }
        Integer reviewedCount = bbsIdeaMapper.countReviewsByReviewerOnIdea(idea.getIdeaId(), currentUserId);
        return reviewedCount != null && reviewedCount > 0;
    }

    @Override
    @Transactional
    public void reviewIdea(Long ideaId, String stage, String action, Integer score, String comment, String rejectReason,
                           String reviewerUserId, String reviewerNickName)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            throw new ServiceException("提案不存在");
        }
        if (STATUS_DRAFT.equals(idea.getStatus()))
        {
            throw new ServiceException("草稿不可评审");
        }
        if (!StringUtils.equals(stage, idea.getReviewStage()))
        {
            throw new ServiceException("当前提案不在该评审环节（当前：" + idea.getReviewStage() + "）");
        }

        if (score == null || score < 1 || score > 10)
        {
            throw new ServiceException("评分必须在 1-10 分之间");
        }

        BbsIdeaReview review = new BbsIdeaReview();
        review.setIdeaId(ideaId);
        review.setStage(stage);
        review.setAction(action);
        review.setScore(score);
        review.setComment(comment);
        review.setRejectReason(rejectReason);
        review.setReviewerUserId(reviewerUserId);
        review.setReviewerNickName(reviewerNickName);
        review.setIsVisibleToAll("0");
        review.setReviewTime(new Date());
        bbsIdeaMapper.insertReview(review);

        BigDecimal avgScore = bbsIdeaMapper.selectAvgScore(ideaId);
        bbsIdeaMapper.updateIdeaAvgScore(ideaId, avgScore);

        if ("reject".equalsIgnoreCase(action))
        {
            if (StringUtils.isBlank(rejectReason))
            {
                throw new ServiceException("驳回必须填写原因");
            }
            // 任意评审人驳回 → 直接驳回提案
            bbsIdeaMapper.updateReviewStage(ideaId, STAGE_NONE, null, rejectReason, STATUS_REJECTED);
            addProgressInternal(ideaId, STATUS_REJECTED, stage + "评审驳回", rejectReason, null, 0, reviewerUserId, reviewerNickName);
            notifyUser(idea.getSubmitUserId(), "【提案被驳回】" + idea.getTitle(),
                    stage + "评审驳回：" + rejectReason + "\n您可重新编辑并提交。", ideaId);
            return;
        }

        // pass — 检查该环节所有指定评审人是否全部通过
        List<String> assignedReviewers = reviewerConfigService.getReviewerUserIdsForStage(stage);
        List<BbsIdeaReview> latestVotes = bbsIdeaMapper.selectLatestVotesPerReviewer(ideaId, stage);

        boolean allPassed = allAssignedReviewersPassed(assignedReviewers, latestVotes);

        if (!allPassed)
        {
            // 还有评审人未投票或未全部通过，等待中
            return;
        }

        // 全部通过，推进到下一环节
        if (STAGE_L1.equals(stage))
        {
            bbsIdeaMapper.updateReviewStage(ideaId, STAGE_L2, addDays(7), null, STATUS_L2_REVIEWING);
            addProgressInternal(ideaId, STATUS_L1_PASSED, "初审全部通过", comment, null, 35, reviewerUserId, reviewerNickName);
            notifyUser(idea.getSubmitUserId(), "【提案初审通过】" + idea.getTitle(), "您的提案已通过初审，进入复审环节", ideaId);
            notifyReviewers(STAGE_L2, ideaId, "【复审待办】" + idea.getTitle(), "您有新的提案待复审，请及时处理");
        }
        else if (STAGE_L2.equals(stage))
        {
            bbsIdeaMapper.updateReviewStage(ideaId, STAGE_L3, addDays(7), null, STATUS_L3_REVIEWING);
            addProgressInternal(ideaId, STATUS_L2_PASSED, "复审全部通过", comment, null, 55, reviewerUserId, reviewerNickName);
            notifyUser(idea.getSubmitUserId(), "【提案复审通过】" + idea.getTitle(), "您的提案已通过复审，进入终审环节", ideaId);
            notifyReviewers(STAGE_L3, ideaId, "【终审待办】" + idea.getTitle(), "您有新的提案待终审，请及时处理");
        }
        else if (STAGE_L3.equals(stage))
        {
            bbsIdeaMapper.updateReviewStage(ideaId, STAGE_NONE, null, null, STATUS_APPROVED);
            bbsIdeaMapper.updateIdeaStatus(ideaId, STATUS_IMPLEMENTING);
            addProgressInternal(ideaId, STATUS_APPROVED, "终审全部通过并立项", comment, null, 70, reviewerUserId, reviewerNickName);
            notifyUser(idea.getSubmitUserId(), "【提案终审通过】" + idea.getTitle(),
                    "您的提案已通过终审并立项，进入落地阶段，您现在可以更新进度", ideaId);
        }
    }

    /**
     * 检查所有指定评审人是否都投了 pass。
     * 若未配置指定评审人（assignedReviewers为空），则只要当前有任意一票 pass 即视为通过（兼容旧配置）。
     */
    private boolean allAssignedReviewersPassed(List<String> assignedReviewers, List<BbsIdeaReview> latestVotes)
    {
        if (assignedReviewers == null || assignedReviewers.isEmpty())
        {
            // 无指定用户配置，只要有人投了pass就算通过
            for (BbsIdeaReview vote : latestVotes)
            {
                if ("pass".equalsIgnoreCase(vote.getAction()))
                {
                    return true;
                }
            }
            return false;
        }
        for (String uid : assignedReviewers)
        {
            boolean hasPassed = false;
            for (BbsIdeaReview vote : latestVotes)
            {
                if (uid.equals(vote.getReviewerUserId()) && "pass".equalsIgnoreCase(vote.getAction()))
                {
                    hasPassed = true;
                    break;
                }
            }
            if (!hasPassed)
            {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public void resubmitIdea(BbsIdeaProposal idea, String userId, String nickName)
    {
        BbsIdeaProposal existing = bbsIdeaMapper.selectIdeaById(idea.getIdeaId());
        if (existing == null)
        {
            throw new ServiceException("提案不存在");
        }
        if (!userId.equals(existing.getSubmitUserId()))
        {
            throw new ServiceException("无权重新提交他人提案");
        }
        if (!STATUS_REJECTED.equals(existing.getStatus()))
        {
            throw new ServiceException("仅被驳回的提案可重新提交");
        }

        validateSubmitForm(idea);

        normalizeIdeaDefaults(idea);
        idea.setIsDraft("0");
        idea.setStatus(STATUS_PENDING_L1);
        idea.setReviewStage(STAGE_L1);
        idea.setReviewDeadline(addDays(7));
        bbsIdeaMapper.updateIdea(idea);

        addProgressInternal(idea.getIdeaId(), STATUS_PENDING_L1, "重新提交", "提案人修改后重新提交，进入初审队列", null, 10, userId, nickName);

        notifyReviewers(STAGE_L1, idea.getIdeaId(), "【初审待办（重提）】" + idea.getTitle(), "提案人已修改并重新提交，请重新评审");
    }

    @Override
    @Transactional
    public void addProgress(Long ideaId, String statusEnum, String stage, String desc, String attachmentUrls, Integer percent,
                            String updaterUserId, String updaterNickName)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            throw new ServiceException("提案不存在");
        }
        // 只有提案人可以更新进度
        if (!updaterUserId.equals(idea.getSubmitUserId()))
        {
            throw new ServiceException("只有提案人可以更新进度");
        }
        // 只有终审通过（立项）后才可更新进度
        if (!STATUS_APPROVED.equals(idea.getStatus()) && !STATUS_IMPLEMENTING.equals(idea.getStatus())
                && !STATUS_PAUSED.equals(idea.getStatus()) && !STATUS_LANDED.equals(idea.getStatus()))
        {
            throw new ServiceException("提案需终审通过立项后方可更新进度");
        }
        addProgressInternal(ideaId, statusEnum, stage, desc, attachmentUrls, percent, updaterUserId, updaterNickName);
        // 通知终审评审人有进度更新（不通知提案人自身，其本人已知晓）
        notifyReviewers(STAGE_L3, ideaId, "【提案进度更新】" + idea.getTitle(), desc);
    }

    /** 内部进度插入，不做权限检查，供系统自动流转调用 */
    private void addProgressInternal(Long ideaId, String statusEnum, String stage, String desc, String attachmentUrls, Integer percent,
                                     String updaterUserId, String updaterNickName)
    {
        BbsIdeaProgress progress = new BbsIdeaProgress();
        progress.setIdeaId(ideaId);
        progress.setStatusEnum(statusEnum);
        progress.setUpdaterUserId(updaterUserId);
        progress.setUpdaterNickName(updaterNickName);
        progress.setProgressStage(stage);
        progress.setProgressDesc(desc);
        progress.setAttachmentUrls(attachmentUrls);
        progress.setProgressPercent(percent == null ? 0 : percent);
        bbsIdeaMapper.insertProgress(progress);

        if (StringUtils.isNotBlank(statusEnum))
        {
            if (STATUS_LANDED.equals(statusEnum))
            {
                bbsIdeaMapper.updateLanded(ideaId, new Date());
            }
            else if (!STATUS_PENDING_L1.equals(statusEnum) && !STATUS_L1_PASSED.equals(statusEnum)
                    && !STATUS_L2_PASSED.equals(statusEnum) && !STATUS_APPROVED.equals(statusEnum)
                    && !STATUS_REJECTED.equals(statusEnum))
            {
                // 仅对真正的状态变更执行更新，进度记录中的中间状态不更新主表
                bbsIdeaMapper.updateIdeaStatus(ideaId, statusEnum);
            }
        }
    }

    @Override
    public void updateIdeaStatus(Long ideaId, String status)
    {
        bbsIdeaMapper.updateIdeaStatus(ideaId, status);
    }

    @Override
    @Transactional
    public void publishResult(Long ideaId, String resultSummary, Integer incentivePoints, String honorBadge, String submitterUserId)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            throw new ServiceException("提案不存在");
        }
        if (!submitterUserId.equals(idea.getSubmitUserId()))
        {
            throw new ServiceException("只有提案人可提交成果公示");
        }
        if (!STATUS_LANDED.equals(idea.getStatus()))
        {
            throw new ServiceException("提案需达到已落地状态方可公示成果");
        }
        // 校验进度是否达到100%
        Integer maxPercent = bbsIdeaMapper.selectMaxProgressPercent(ideaId);
        if (maxPercent == null || maxPercent < 100)
        {
            throw new ServiceException("提案进度需达到100%方可提交成果公示");
        }

        int points = incentivePoints == null ? 0 : Math.max(incentivePoints, 0);
        bbsIdeaMapper.publishIdeaResult(ideaId, resultSummary, points, honorBadge);

        if (points > 0)
        {
            sysUserMapper.incrementBbsPoints(idea.getSubmitUserId(), points);
            BbsPointRecord record = new BbsPointRecord();
            record.setUserId(idea.getSubmitUserId());
            record.setActionType("IDEA_INCENTIVE");
            record.setBizType("idea_proposal");
            record.setBizId(ideaId);
            record.setPointChange(points);
            record.setRemark("创意提案公示激励积分");
            record.setDelFlag("0");
            bbsPointRecordMapper.insertIgnore(record);
        }

        addProgressInternal(ideaId, STATUS_PUBLICIZED, "成果公示", resultSummary, null, 100, "system", "系统");
        notifyUser(idea.getSubmitUserId(), "【提案成果已公示】" + idea.getTitle(), resultSummary, ideaId);
    }

    @Override
    public boolean likePublicizedIdea(Long ideaId, String userId)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            throw new ServiceException("提案不存在");
        }
        if (!"1".equals(idea.getIsPublicized()))
        {
            throw new ServiceException("仅已公示提案支持点赞");
        }
        int removed = bbsIdeaMapper.deleteIdeaUserAction(ideaId, userId, "LIKE");
        if (removed > 0)
        {
            bbsIdeaMapper.decrementLikeCount(ideaId);
            return false;
        }
        int changed = bbsIdeaMapper.insertIdeaUserActionIgnore(ideaId, userId, "LIKE");
        if (changed > 0)
        {
            bbsIdeaMapper.incrementLikeCount(ideaId);
            return true;
        }
        return true;
    }

    @Override
    public boolean sharePublicizedIdea(Long ideaId, String userId)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            throw new ServiceException("提案不存在");
        }
        if (!"1".equals(idea.getIsPublicized()))
        {
            throw new ServiceException("仅已公示提案支持分享");
        }
        int changed = bbsIdeaMapper.insertIdeaUserActionIgnore(ideaId, userId, "SHARE");
        if (changed > 0)
        {
            bbsIdeaMapper.incrementShareCount(ideaId);
            return true;
        }
        return false;
    }

    private void validateSubmitForm(BbsIdeaProposal idea)
    {
        if (StringUtils.isBlank(idea.getTitle())
                || StringUtils.isBlank(idea.getCategory())
                || StringUtils.isBlank(idea.getBackgroundText())
                || StringUtils.isBlank(idea.getPlanText())
                || StringUtils.isBlank(idea.getExpectedValue()))
        {
            throw new ServiceException("提案名称、分类、背景、实施方案、预期价值为必填项");
        }
        if (StringUtils.isBlank(idea.getVisibilityScope()))
        {
            throw new ServiceException("可见范围为必填项");
        }
    }

    private void normalizeIdeaDefaults(BbsIdeaProposal idea)
    {
        if (StringUtils.isBlank(idea.getCategory()))
        {
            idea.setCategory("other");
        }
        if (StringUtils.isBlank(idea.getVisibilityScope()))
        {
            idea.setVisibilityScope("review_group");
        }
        if (idea.getIncentivePoints() == null)
        {
            idea.setIncentivePoints(0);
        }
    }

    private Date addDays(int days)
    {
        long now = System.currentTimeMillis();
        return new Date(now + days * 24L * 3600L * 1000L);
    }

    private void notifyUser(String userId, String title, String content, Long ideaId)
    {
        if (StringUtils.isBlank(userId))
        {
            return;
        }
        BbsNotification notification = new BbsNotification();
        notification.setUserId(userId);
        notification.setType("5");
        notification.setTitle(title);
        notification.setContent(content);
        notification.setTargetType("6");
        notification.setTargetId(ideaId != null ? ideaId : 0L);
        notification.setFromUserId("system");
        notification.setFromNickName("系统");
        notification.setIsRead("0");
        bbsNotificationMapper.insertBbsNotification(notification);
    }

    /** 通知某环节所有配置的指定处理人 */
    private void notifyReviewers(String stage, Long ideaId, String title, String content)
    {
        List<String> reviewerIds = reviewerConfigService.getReviewerUserIdsForStage(stage);
        for (String uid : reviewerIds)
        {
            notifyUser(uid, title, content, ideaId);
        }
    }

    @Override
    public List<BbsIdeaProposal> listIdeasForManage(String status, String category, String keyword, String submitNickName)
    {
        return bbsIdeaMapper.selectIdeaManageList(status, category, keyword, submitNickName);
    }

    @Override
    public BbsIdeaProposal getIdeaForManage(Long ideaId)
    {
        BbsIdeaProposal idea = bbsIdeaMapper.selectIdeaById(ideaId);
        if (idea == null)
        {
            throw new ServiceException("提案不存在或已删除");
        }
        idea.setReviews(bbsIdeaMapper.selectReviewList(ideaId));
        idea.setProgressList(bbsIdeaMapper.selectProgressList(ideaId));
        return idea;
    }

    @Override
    public int deleteIdeas(Long[] ideaIds)
    {
        return bbsIdeaMapper.logicDeleteIdeas(ideaIds);
    }

    @Override
    public void cancelPublicize(Long ideaId)
    {
        if (bbsIdeaMapper.cancelPublicize(ideaId) == 0)
        {
            throw new ServiceException("提案不存在或当前未公示");
        }
        addProgressInternal(ideaId, "LANDED", "取消公示", "管理员取消成果公示", null, 100, "system", "系统");
    }
}
