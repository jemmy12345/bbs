package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.BbsSensitiveWord;

/**
 * 敏感词 服务层
 * 
 * @author ruoyi
 */
public interface IBbsSensitiveWordService
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
     * 批量删除敏感词
     * 
     * @param wordIds 需要删除的敏感词ID
     * @return 结果
     */
    public int deleteBbsSensitiveWordByIds(Long[] wordIds);

    /**
     * 删除敏感词信息
     * 
     * @param wordId 敏感词ID
     * @return 结果
     */
    public int deleteBbsSensitiveWordById(Long wordId);

    /**
     * 检测文本中是否包含敏感词
     * 
     * @param text 待检测文本
     * @return 检测结果，包含敏感词列表
     */
    public List<String> checkSensitiveWords(String text);
}
