package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysNotice;

/**
 * 通知公告表 数据层
 * 
 * @author Smart BBS
 */
public interface SysNoticeMapper
{
    /**
     * 查询公告信息
     * 
     * @param noticeId 公告ID
     * @return 公告信息
     */
    public SysNotice selectNoticeById(Long noticeId);

    /**
     * 查询公告列表
     * 
     * @param notice 公告信息
     * @return 公告集合
     */
    public List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 新增公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int insertNotice(SysNotice notice);

    /**
     * 修改公告
     * 
     * @param notice 公告信息
     * @return 结果
     */
    public int updateNotice(SysNotice notice);

    /**
     * 批量删除公告
     * 
     * @param noticeId 公告ID
     * @return 结果
     */
    public int deleteNoticeById(Long noticeId);

    /**
     * 批量删除公告信息
     * 
     * @param noticeIds 需要删除的公告ID
     * @return 结果
     */
    public int deleteNoticeByIds(Long[] noticeIds);

    /**
     * 查询用户未读的有效公告（状态正常且未读），按创建时间倒序
     */
    public List<SysNotice> selectUnreadNotices(@org.apache.ibatis.annotations.Param("userId") String userId);

    /**
     * 用户端：查询有效公告列表（含当前用户已读标识），可按类型过滤
     */
    public List<SysNotice> selectMyNoticeList(@org.apache.ibatis.annotations.Param("userId") String userId,
                                              @org.apache.ibatis.annotations.Param("noticeType") String noticeType);

    /**
     * 标记公告已读（重复标记忽略）
     */
    public int insertNoticeRead(@org.apache.ibatis.annotations.Param("noticeId") Long noticeId,
                                @org.apache.ibatis.annotations.Param("userId") String userId);
}
