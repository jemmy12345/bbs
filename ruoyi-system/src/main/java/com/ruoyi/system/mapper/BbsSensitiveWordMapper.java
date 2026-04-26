package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.BbsSensitiveWord;

/**
 * 敏感词表 数据层
 * 
 * @author ruoyi
 */
public interface BbsSensitiveWordMapper
{
    /**
     * 查询敏感词信息
     * 
     * @param wordId 敏感词ID
     * @return 敏感词信息
     */
    public BbsSensitiveWord selectBbsSensitiveWordById(Long wordId);

    /**
     * 查询敏感词列表
     * 
     * @param bbsSensitiveWord 敏感词信息
     * @return 敏感词集合
     */
    public List<BbsSensitiveWord> selectBbsSensitiveWordList(BbsSensitiveWord bbsSensitiveWord);

    /**
     * 查询所有启用的敏感词列表
     * 
     * @return 敏感词集合
     */
    public List<String> selectEnabledSensitiveWordList();

    /**
     * 新增敏感词
     * 
     * @param bbsSensitiveWord 敏感词信息
     * @return 结果
     */
    public int insertBbsSensitiveWord(BbsSensitiveWord bbsSensitiveWord);

    /**
     * 修改敏感词
     * 
     * @param bbsSensitiveWord 敏感词信息
     * @return 结果
     */
    public int updateBbsSensitiveWord(BbsSensitiveWord bbsSensitiveWord);

    /**
     * 删除敏感词
     * 
     * @param wordId 敏感词ID
     * @return 结果
     */
    public int deleteBbsSensitiveWordById(Long wordId);

    /**
     * 批量删除敏感词
     * 
     * @param wordIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteBbsSensitiveWordByIds(Long[] wordIds);
}
