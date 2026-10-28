package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.BbsIdeaProgress;
import com.ruoyi.system.domain.BbsIdeaProposal;
import com.ruoyi.system.domain.BbsIdeaReview;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface BbsIdeaMapper
{
    int insertIdea(BbsIdeaProposal idea);

    int updateIdea(BbsIdeaProposal idea);

    List<BbsIdeaProposal> selectIdeaList(@Param("status") String status,
                                         @Param("category") String category,
                                         @Param("keyword") String keyword,
                                         @Param("isPublicized") String isPublicized,
                                         @Param("currentUserId") String currentUserId,
                                         @Param("isAdmin") Integer isAdmin,
                                         @Param("canReviewAll") Integer canReviewAll,
                                         @Param("mineOnly") Integer mineOnly);

    List<BbsIdeaProposal> selectPublicizedWall(@Param("sortBy") String sortBy,
                                               @Param("category") String category,
                                               @Param("keyword") String keyword,
                                               @Param("currentUserId") String currentUserId);

    Integer countTodaySubmissions(@Param("submitUserId") String submitUserId);

    Integer countDuplicateToday(@Param("submitUserId") String submitUserId,
                                @Param("title") String title);

    BbsIdeaProposal selectIdeaById(@Param("ideaId") Long ideaId);

    int insertReview(BbsIdeaReview review);

    List<BbsIdeaReview> selectReviewList(@Param("ideaId") Long ideaId);

    int updateIdeaAvgScore(@Param("ideaId") Long ideaId, @Param("avgScore") BigDecimal avgScore);

    BigDecimal selectAvgScore(@Param("ideaId") Long ideaId);

    int insertProgress(BbsIdeaProgress progress);

    Integer selectMaxProgressPercent(@Param("ideaId") Long ideaId);

    List<BbsIdeaProgress> selectProgressList(@Param("ideaId") Long ideaId);

    int updateIdeaStatus(@Param("ideaId") Long ideaId, @Param("status") String status);

    int updateReviewStage(@Param("ideaId") Long ideaId,
                          @Param("reviewStage") String reviewStage,
                          @Param("reviewDeadline") java.util.Date reviewDeadline,
                          @Param("rejectReason") String rejectReason,
                          @Param("status") String status);

    int updateLanded(@Param("ideaId") Long ideaId, @Param("landingTime") java.util.Date landingTime);

    int publishIdeaResult(@Param("ideaId") Long ideaId,
                          @Param("resultSummary") String resultSummary,
                          @Param("incentivePoints") Integer incentivePoints,
                          @Param("honorBadge") String honorBadge);

    int incrementLikeCount(@Param("ideaId") Long ideaId);

    int decrementLikeCount(@Param("ideaId") Long ideaId);

    int incrementShareCount(@Param("ideaId") Long ideaId);

    /** 查询某提案某环节各评审人最新一次投票（action: pass/reject） */
    List<BbsIdeaReview> selectLatestVotesPerReviewer(@Param("ideaId") Long ideaId, @Param("stage") String stage);

    /** 检查某用户是否已对某提案某环节投过票 */
    BbsIdeaReview selectLatestVoteByReviewer(@Param("ideaId") Long ideaId,
                                             @Param("stage") String stage,
                                             @Param("reviewerUserId") String reviewerUserId);

    Integer countReviewsByReviewerOnIdea(@Param("ideaId") Long ideaId,
                                         @Param("reviewerUserId") String reviewerUserId);

    int insertIdeaUserActionIgnore(@Param("ideaId") Long ideaId,
                                   @Param("userId") String userId,
                                   @Param("actionType") String actionType);

    int deleteIdeaUserAction(@Param("ideaId") Long ideaId,
                             @Param("userId") String userId,
                             @Param("actionType") String actionType);

    /** 管理端：查询全部未删除提案（不受可见范围限制，含草稿） */
    List<BbsIdeaProposal> selectIdeaManageList(@Param("status") String status,
                                               @Param("category") String category,
                                               @Param("keyword") String keyword,
                                               @Param("submitNickName") String submitNickName);

    /** 管理端：逻辑删除提案 */
    int logicDeleteIdeas(@Param("ideaIds") Long[] ideaIds);

    /** 管理端：取消公示，恢复为已落地状态；仅对已公示提案生效 */
    int cancelPublicize(@Param("ideaId") Long ideaId);
}
