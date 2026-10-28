package com.ruoyi.system.service;

import com.ruoyi.system.domain.BbsIdeaProposal;

import java.util.List;

public interface IBbsIdeaService
{
    Long saveOrSubmitIdea(BbsIdeaProposal idea, String userId, String nickName, boolean submit);

    void updateDraft(BbsIdeaProposal idea, String currentUserId);

    List<BbsIdeaProposal> listIdeas(String status, String category, String keyword, String isPublicized,
                                    String currentUserId, boolean canReviewAll, Boolean mine);

    List<BbsIdeaProposal> listPublicizedWall(String sortBy, String category, String keyword);

    BbsIdeaProposal getIdeaDetail(Long ideaId, String currentUserId, boolean canReviewAll);

    void reviewIdea(Long ideaId, String stage, String action, Integer score, String comment, String rejectReason,
                    String reviewerUserId, String reviewerNickName);

    void resubmitIdea(BbsIdeaProposal idea, String userId, String nickName);

    void addProgress(Long ideaId, String statusEnum, String stage, String desc, String attachmentUrls, Integer percent,
                     String updaterUserId, String updaterNickName);

    void updateIdeaStatus(Long ideaId, String status);

    void publishResult(Long ideaId, String resultSummary, Integer incentivePoints, String honorBadge, String submitterUserId);

    void likePublicizedIdea(Long ideaId);

    void sharePublicizedIdea(Long ideaId);
}
