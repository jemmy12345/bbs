package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.BbsSensitiveWord;
import com.ruoyi.system.mapper.BbsSensitiveWordMapper;
import com.ruoyi.system.service.IBbsSensitiveWordService;

/**
 * 敏感词 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class BbsSensitiveWordServiceImpl implements IBbsSensitiveWordService
{
    @Autowired
    private BbsSensitiveWordMapper bbsSensitiveWordMapper;

    // 敏感词缓存，提高检测性能
    private List<String> sensitiveWordCache = null;
    private long cacheUpdateTime = 0;
    private static final long CACHE_EXPIRE_TIME = 60000; // 缓存1分钟

    /**
     * 查询敏感词信息
     * 
     * @param wordId 敏感词ID
     * @return 敏感词信息
     */
    @Override
    public BbsSensitiveWord selectBbsSensitiveWordById(Long wordId)
    {
        return bbsSensitiveWordMapper.selectBbsSensitiveWordById(wordId);
    }

    /**
     * 查询敏感词列表
     * 
     * @param bbsSensitiveWord 敏感词信息
     * @return 敏感词集合
     */
    @Override
    public List<BbsSensitiveWord> selectBbsSensitiveWordList(BbsSensitiveWord bbsSensitiveWord)
    {
        return bbsSensitiveWordMapper.selectBbsSensitiveWordList(bbsSensitiveWord);
    }

    /**
     * 新增敏感词
     * 
     * @param bbsSensitiveWord 敏感词信息
     * @return 结果
     */
    @Override
    public int insertBbsSensitiveWord(BbsSensitiveWord bbsSensitiveWord)
    {
        // 清除缓存
        clearCache();
        return bbsSensitiveWordMapper.insertBbsSensitiveWord(bbsSensitiveWord);
    }

    /**
     * 修改敏感词
     * 
     * @param bbsSensitiveWord 敏感词信息
     * @return 结果
     */
    @Override
    public int updateBbsSensitiveWord(BbsSensitiveWord bbsSensitiveWord)
    {
        // 清除缓存
        clearCache();
        return bbsSensitiveWordMapper.updateBbsSensitiveWord(bbsSensitiveWord);
    }

    /**
     * 批量删除敏感词
     * 
     * @param wordIds 需要删除的敏感词ID
     * @return 结果
     */
    @Override
    public int deleteBbsSensitiveWordByIds(Long[] wordIds)
    {
        // 清除缓存
        clearCache();
        return bbsSensitiveWordMapper.deleteBbsSensitiveWordByIds(wordIds);
    }

    /**
     * 删除敏感词信息
     * 
     * @param wordId 敏感词ID
     * @return 结果
     */
    @Override
    public int deleteBbsSensitiveWordById(Long wordId)
    {
        // 清除缓存
        clearCache();
        return bbsSensitiveWordMapper.deleteBbsSensitiveWordById(wordId);
    }

    /**
     * 检测文本中是否包含敏感词
     * 
     * @param text 待检测文本
     * @return 检测结果，包含敏感词列表
     */
    @Override
    public List<String> checkSensitiveWords(String text)
    {
        if (text == null || text.trim().isEmpty())
        {
            return new ArrayList<>();
        }

        // 获取敏感词列表（带缓存）
        List<String> sensitiveWords = getSensitiveWordList();
        
        if (sensitiveWords == null || sensitiveWords.isEmpty())
        {
            return new ArrayList<>();
        }

        // 去除HTML标签，只检测纯文本
        String plainText = text.replaceAll("<[^>]+>", "");
        
        List<String> foundWords = new ArrayList<>();
        
        // 遍历敏感词进行检测
        for (String word : sensitiveWords)
        {
            if (word == null || word.trim().isEmpty())
            {
                continue;
            }
            
            // 使用正则表达式进行匹配（支持部分匹配）
            Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(plainText).find())
            {
                if (!foundWords.contains(word))
                {
                    foundWords.add(word);
                }
            }
        }
        
        return foundWords;
    }

    /**
     * 获取敏感词列表（带缓存）
     * 
     * @return 敏感词列表
     */
    private List<String> getSensitiveWordList()
    {
        long currentTime = System.currentTimeMillis();
        
        // 如果缓存存在且未过期，直接返回
        if (sensitiveWordCache != null && (currentTime - cacheUpdateTime) < CACHE_EXPIRE_TIME)
        {
            return sensitiveWordCache;
        }
        
        // 从数据库加载敏感词列表
        sensitiveWordCache = bbsSensitiveWordMapper.selectEnabledSensitiveWordList();
        cacheUpdateTime = currentTime;
        
        return sensitiveWordCache;
    }

    /**
     * 清除缓存
     */
    private void clearCache()
    {
        sensitiveWordCache = null;
        cacheUpdateTime = 0;
    }
}
