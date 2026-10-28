package com.ruoyi.system.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.BbsDislike;
import com.ruoyi.system.mapper.BbsDislikeMapper;
import com.ruoyi.system.service.IBbsDislikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 点踩Service业务层处理
 * 
 * @author simonyang
 * @date 2026-04-08
 */
@Service
public class BbsDislikeServiceImpl implements IBbsDislikeService
{
    @Autowired
    private BbsDislikeMapper bbsDislikeMapper;


    @Override
    @Transactional
    public int dislike(BbsDislike bbsDislike) {
        String targetType = bbsDislike.getTargetType();
        Long targetId = bbsDislike.getTargetId();
        if(!StringUtils.hasText(targetType)){
            throw new RuntimeException("参数错误");
        }
        if(targetId == null){
            throw new RuntimeException("参数错误");
        }

        bbsDislike.setUserId(SecurityUtils.getLoginUser().getUserId());

        // 查询是否已经点踩
        List<BbsDislike> existList = bbsDislikeMapper.selectBbsDislikeList(bbsDislike);
        if (existList != null && !existList.isEmpty())
        {
            // 已点踩，取消点踩
            return bbsDislikeMapper.deleteBbsDislikeByDislikeId(existList.get(0).getDislikeId());
        }
        else
        {
            // 未点踩，新增点踩记录
            bbsDislike.setCreateTime(new Date());
            return bbsDislikeMapper.insertBbsDislike(bbsDislike);
        }
    }
}
