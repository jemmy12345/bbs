package com.ruoyi.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * 用户每日签到实体
 */
public class BbsCheckin
{
    private static final long serialVersionUID = 1L;

    private Long checkinId;

    private String userId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date checkinDate;

    private Integer streak;

    private Integer pointsEarned;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Long getCheckinId() { return checkinId; }
    public void setCheckinId(Long checkinId) { this.checkinId = checkinId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Date getCheckinDate() { return checkinDate; }
    public void setCheckinDate(Date checkinDate) { this.checkinDate = checkinDate; }

    public Integer getStreak() { return streak; }
    public void setStreak(Integer streak) { this.streak = streak; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
