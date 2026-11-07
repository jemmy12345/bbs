package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsIdeaProposal;

import java.util.List;

public interface IBbsIdeaService
{
    Long saveOrSubmitIdea(BbsIdeaProposal idea, String userId, String nickName, boolean submit);

    void updateDraft(BbsIdeaProposal idea, String currentUserId);

    List<BbsIdeaProposal> listIdeas(String status, String category, String keyword, String isPublicized,
                                    String currentUserId, boolean isAdmin, boolean canReviewAll, Boolean mine);

    List<BbsIdeaProposal> listPublicizedWall(String sortBy, String category, String keyword, String currentUserId);

    /** 对已公示提案点赞/取消点赞，返回 {liked, likeCount} */
    java.util.Map<String, Object> toggleLike(Long ideaId, String userId);

    /** 分享已公示提案（同一用户只计一次），返回 {shareCount} */
    java.util.Map<String, Object> sharePublicizedIdea(Long ideaId, String userId);

    BbsIdeaProposal getIdeaDetail(Long ideaId, String currentUserId, boolean isAdmin, boolean canReviewAll);

    void reviewIdea(Long ideaId, String stage, String action, Integer score, String comment, String rejectReason,
                    String reviewerUserId, String reviewerNickName);

    void resubmitIdea(BbsIdeaProposal idea, String userId, String nickName);

    void addProgress(Long ideaId, String statusEnum, String stage, String desc, String attachmentUrls, Integer percent,
                     String updaterUserId, String updaterNickName);

    void updateIdeaStatus(Long ideaId, String status);

    void publishResult(Long ideaId, String resultSummary, String honorBadge, String submitterUserId);

    List<BbsIdeaProposal> listIdeasForManage(String status, String category, String keyword, String submitNickName);

    BbsIdeaProposal getIdeaForManage(Long ideaId);

    int deleteIdeas(Long[] ideaIds);

    void cancelPublicize(Long ideaId);
}
