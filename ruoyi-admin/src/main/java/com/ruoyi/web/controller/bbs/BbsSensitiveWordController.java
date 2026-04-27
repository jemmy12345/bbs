package com.ruoyi.web.controller.bbs;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.AdminLog;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.AdminOperationType;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.AdminLogContext;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.BbsSensitiveWord;
import com.ruoyi.system.service.IBbsSensitiveWordService;

/**
 * 敏感词 信息操作处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/bbs/sensitive")
@Api(tags = "敏感词")
public class BbsSensitiveWordController extends BaseController
{
    @Autowired
    private IBbsSensitiveWordService bbsSensitiveWordService;

    /**
     * 查询敏感词列表
     */
    @ApiOperation("查询敏感词列表")
    @PreAuthorize("@ss.hasPermi('bbs:sensitive:list')")
    @GetMapping("/list")
    public TableDataInfo list(BbsSensitiveWord bbsSensitiveWord)
    {
        startPage();
        List<BbsSensitiveWord> list = bbsSensitiveWordService.selectBbsSensitiveWordList(bbsSensitiveWord);
        return getDataTable(list);
    }

    /**
     * 导出敏感词列表
     */
    @ApiOperation("导出敏感词列表")
    @PreAuthorize("@ss.hasPermi('bbs:sensitive:export')")
    @Log(title = "敏感词管理", businessType = BusinessType.EXPORT)
    @AdminLog(module = "敏感词管理", operationType = AdminOperationType.EXPORT, description = "导出敏感词列表")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BbsSensitiveWord bbsSensitiveWord)
    {
        List<BbsSensitiveWord> list = bbsSensitiveWordService.selectBbsSensitiveWordList(bbsSensitiveWord);
        ExcelUtil<BbsSensitiveWord> util = new ExcelUtil<BbsSensitiveWord>(BbsSensitiveWord.class);
        util.exportExcel(response, list, "敏感词数据");
    }

    /**
     * 根据敏感词编号获取详细信息
     */
    @ApiOperation("根据敏感词编号获取详细信息")
    @PreAuthorize("@ss.hasPermi('bbs:sensitive:query')")
    @GetMapping(value = "/{wordId}")
    public AjaxResult getInfo(@PathVariable Long wordId)
    {
        return success(bbsSensitiveWordService.selectBbsSensitiveWordById(wordId));
    }

    /**
     * 新增敏感词
     */
    @ApiOperation("新增敏感词")
    @PreAuthorize("@ss.hasPermi('bbs:sensitive:add')")
    @Log(title = "敏感词管理", businessType = BusinessType.INSERT)
    @AdminLog(module = "敏感词管理", operationType = AdminOperationType.ADD)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody BbsSensitiveWord bbsSensitiveWord)
    {
        AdminLogContext.setDescription("新增敏感词：「" + bbsSensitiveWord.getWord() + "」");
        bbsSensitiveWord.setCreateBy(getUsername());
        bbsSensitiveWord.setCreateTime(new Date());
        return toAjax(bbsSensitiveWordService.insertBbsSensitiveWord(bbsSensitiveWord));
    }

    /**
     * 修改敏感词
     */
    @ApiOperation("修改敏感词")
    @PreAuthorize("@ss.hasPermi('bbs:sensitive:edit')")
    @Log(title = "敏感词管理", businessType = BusinessType.UPDATE)
    @AdminLog(module = "敏感词管理", operationType = AdminOperationType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody BbsSensitiveWord bbsSensitiveWord)
    {
        AdminLogContext.setDescription("修改敏感词：「" + bbsSensitiveWord.getWord() + "」");
        bbsSensitiveWord.setUpdateBy(getUsername());
        return toAjax(bbsSensitiveWordService.updateBbsSensitiveWord(bbsSensitiveWord));
    }

    /**
     * 删除敏感词
     */
    @ApiOperation("删除敏感词")
    @PreAuthorize("@ss.hasPermi('bbs:sensitive:remove')")
    @Log(title = "敏感词管理", businessType = BusinessType.DELETE)
    @AdminLog(module = "敏感词管理", operationType = AdminOperationType.DELETE)
    @DeleteMapping("/{wordIds}")
    public AjaxResult remove(@PathVariable Long[] wordIds)
    {
        // 查询待删除敏感词用于操作日志
        StringBuilder words = new StringBuilder();
        for (Long wordId : wordIds)
        {
            BbsSensitiveWord word = bbsSensitiveWordService.selectBbsSensitiveWordById(wordId);
            if (word != null)
            {
                if (words.length() > 0) words.append("、");
                words.append(word.getWord());
            }
        }
        AdminLogContext.setDescription("删除敏感词：「" + words.toString() + "」");
        return toAjax(bbsSensitiveWordService.deleteBbsSensitiveWordByIds(wordIds));
    }

    /**
     * 检测文本中的敏感词
     */
    @ApiOperation("检测文本中的敏感词")
    @PostMapping("/check")
    public AjaxResult checkSensitiveWords(@RequestBody JSONObject text)
    {
        List<String> sensitiveWords = bbsSensitiveWordService.checkSensitiveWords(text.getString("text"));
        AjaxResult ajax = AjaxResult.success();
        boolean hit = sensitiveWords != null && !sensitiveWords.isEmpty();
        ajax.put("hit", hit);
        ajax.put("words", sensitiveWords);
        ajax.put("message", hit ? "内容包含敏感词：" + String.join("、", sensitiveWords) : "内容检测通过");
        return ajax;
    }
}
