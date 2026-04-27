package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.BbsCheckin;
import com.ruoyi.system.domain.BbsPointRecord;
import com.ruoyi.system.mapper.BbsCheckinMapper;
import com.ruoyi.system.mapper.BbsPointRecordMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.IBbsCheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 每日签到 Service 实现
 */
@Service
public class BbsCheckinServiceImpl implements IBbsCheckinService
{
    /** 基础签到积分 */
    private static final int BASE_POINTS = 2;
    /** 连续签到 ≥3 天额外奖励 */
    private static final int STREAK3_BONUS = 1;
    /** 连续签到 ≥7 天额外奖励（叠加）*/
    private static final int STREAK7_BONUS = 2;
    /** 连续签到 ≥30 天额外奖励（叠加）*/
    private static final int STREAK30_BONUS = 5;

    @Autowired
    private BbsCheckinMapper bbsCheckinMapper;

    @Autowired
    private BbsPointRecordMapper bbsPointRecordMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    @Transactional
    public Map<String, Object> doCheckin(String userId)
    {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());

        // 检查今天是否已签到
        BbsCheckin existing = bbsCheckinMapper.selectByUserAndDate(userId, todayDate);
        if (existing != null)
        {
            result.put("alreadyChecked", true);
            result.put("streak", existing.getStreak());
            result.put("pointsEarned", existing.getPointsEarned());
            return result;
        }

        // 计算连续签到天数
        BbsCheckin latest = bbsCheckinMapper.selectLatestByUser(userId);
        int streak = 1;
        if (latest != null)
        {
            LocalDate lastDate = latest.getCheckinDate().toInstant()
                    .atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(lastDate, today);
            if (daysBetween == 1)
            {
                // 昨天签到，连续天数+1
                streak = latest.getStreak() + 1;
            }
            // daysBetween > 1 则连续中断，streak 重置为 1
        }

        // 计算本次积分
        int points = BASE_POINTS;
        if (streak >= 30) points += STREAK30_BONUS;
        else if (streak >= 7) points += STREAK7_BONUS;
        else if (streak >= 3) points += STREAK3_BONUS;

        // 写签到记录
        BbsCheckin checkin = new BbsCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(todayDate);
        checkin.setStreak(streak);
        checkin.setPointsEarned(points);
        bbsCheckinMapper.insert(checkin);

        // 写积分流水（INSERT IGNORE 防重）
        BbsPointRecord record = new BbsPointRecord();
        record.setUserId(userId);
        record.setActionType("CHECKIN");
        record.setBizType("checkin");
        record.setBizId(null);
        record.setPointChange(points);
        record.setRemark("每日签到奖励积分（连续" + streak + "天）");
        record.setDelFlag("0");
        bbsPointRecordMapper.insertIgnore(record);

        // 累加用户积分汇总
        sysUserMapper.incrementBbsPoints(userId, points);

        result.put("alreadyChecked", false);
        result.put("streak", streak);
        result.put("pointsEarned", points);
        return result;
    }

    @Override
    public Map<String, Object> getTodayStatus(String userId)
    {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        Date todayDate = Date.from(today.atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant());
        BbsCheckin existing = bbsCheckinMapper.selectByUserAndDate(userId, todayDate);
        if (existing != null)
        {
            result.put("checked", true);
            result.put("streak", existing.getStreak());
            result.put("pointsEarned", existing.getPointsEarned());
        }
        else
        {
            result.put("checked", false);
            // 取最新连续天数
            BbsCheckin latest = bbsCheckinMapper.selectLatestByUser(userId);
            if (latest != null)
            {
                LocalDate lastDate = latest.getCheckinDate().toInstant()
                        .atZone(ZoneId.of("Asia/Shanghai")).toLocalDate();
                long daysBetween = ChronoUnit.DAYS.between(lastDate, today);
                result.put("streak", daysBetween == 1 ? latest.getStreak() : 0);
            }
            else
            {
                result.put("streak", 0);
            }
            result.put("pointsEarned", 0);
        }
        return result;
    }
}
