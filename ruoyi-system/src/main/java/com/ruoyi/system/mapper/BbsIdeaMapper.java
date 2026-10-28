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
                                         @Param("canReviewAll") Integer canReviewAll,
                                         @Param("mineOnly") Integer mineOnly);

    List<BbsIdeaProposal> selectPublicizedWall(@Param("sortBy") String sortBy,
                                               @Param("category") String category,
                                               @Param("keyword") String keyword);

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

    int incrementShareCount(@Param("ideaId") Long ideaId);

    /** 查询某提案某环节各评审人最新一次投票（action: pass/reject） */
    List<BbsIdeaReview> selectLatestVotesPerReviewer(@Param("ideaId") Long ideaId, @Param("stage") String stage);

    /** 检查某用户是否已对某提案某环节投过票 */
    BbsIdeaReview selectLatestVoteByReviewer(@Param("ideaId") Long ideaId,
                                             @Param("stage") String stage,
                                             @Param("reviewerUserId") String reviewerUserId);
}
