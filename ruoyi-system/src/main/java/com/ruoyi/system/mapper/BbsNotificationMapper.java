package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsNotification;

/**
 * 通知表 数据层
 * 
 * @author ruoyi
 */
public interface BbsNotificationMapper
{
    /**
     * 查询通知信息
     * 
     * @param notificationId 通知ID
     * @return 通知信息
     */
    public BbsNotification selectBbsNotificationById(Long notificationId);

    /**
     * 查询通知列表
     * 
     * @param bbsNotification 通知信息
     * @return 通知集合
     */
    public List<BbsNotification> selectBbsNotificationList(BbsNotification bbsNotification);

    /**
     * 新增通知
     * 
     * @param bbsNotification 通知信息
     * @return 结果
     */
    public int insertBbsNotification(BbsNotification bbsNotification);

    /**
     * 修改通知
     * 
     * @param bbsNotification 通知信息
     * @return 结果
     */
    public int updateBbsNotification(BbsNotification bbsNotification);

    /**
     * 删除通知
     * 
     * @param notificationId 通知ID
     * @return 结果
     */
    public int deleteBbsNotificationById(Long notificationId);

    /**
     * 批量删除通知
     * 
     * @param notificationIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsNotificationByIds(Long[] notificationIds);

    /**
     * 标记为已读
     * 
     * @param notificationId 通知ID
     * @return 结果
     */
    public int markAsRead(Long notificationId);

    /**
     * 全部标记为已读
     * 
     * @param userId 用户ID
     * @return 结果
     */
    public int markAllAsRead(String userId);

    /**
     * 统计未读通知数
     * 
     * @param userId 用户ID
     * @return 未读通知数
     */
    public int countUnread(String userId);
}
