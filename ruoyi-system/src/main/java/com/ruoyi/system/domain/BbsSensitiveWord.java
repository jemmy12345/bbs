package com.ruoyi.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 敏感词表 bbs_sensitive_word
 * 
 * @author ruoyi
 */
public class BbsSensitiveWord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 敏感词ID */
    @Excel(name = "敏感词ID", cellType = Excel.ColumnType.NUMERIC)
    private Long wordId;

    /** 敏感词内容 */
    @Excel(name = "敏感词内容")
    private String word;

    /** 敏感词类型（1-政治敏感 2-色情低俗 3-暴力血腥 4-广告营销 5-其他） */
    @Excel(name = "敏感词类型", readConverterExp = "1=政治敏感,2=色情低俗,3=暴力血腥,4=广告营销,5=其他")
    private String wordType;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getWordId()
    {
        return wordId;
    }

    public void setWordId(Long wordId)
    {
        this.wordId = wordId;
    }

    @NotBlank(message = "敏感词内容不能为空")
    @Size(min = 0, max = 100, message = "敏感词内容长度不能超过100个字符")
    public String getWord()
    {
        return word;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public String getWordType()
    {
        return wordType;
    }

    public void setWordType(String wordType)
    {
        this.wordType = wordType;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("wordId", getWordId())
            .append("word", getWord())
            .append("wordType", getWordType())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
