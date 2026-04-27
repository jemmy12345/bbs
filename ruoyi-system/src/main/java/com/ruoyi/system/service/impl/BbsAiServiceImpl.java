package com.ruoyi.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.ai.BbsAiModerationResult;
import com.ruoyi.system.service.IBbsAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 豆包大模型能力服务
 */
@Service
public class BbsAiServiceImpl implements IBbsAiService
{
    private static final Logger log = LoggerFactory.getLogger(BbsAiServiceImpl.class);

    @Value("${ai.doubao.enabled:false}")
    private boolean enabled;

    @Value("${ai.doubao.apiUrl:https://ark.cn-beijing.volces.com/api/v3/chat/completions}")
    private String apiUrl;

    @Value("${ai.doubao.apiKey:}")
    private String apiKey;

    @Value("${ai.doubao.model:}")
    private String model;

    @Value("${ai.doubao.timeoutMillis:20000}")
    private int timeoutMillis;

    @Override
    public BbsAiModerationResult moderatePost(String title, String content)
    {
        BbsAiModerationResult result = new BbsAiModerationResult();
        if (!enabled || StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(model))
        {
            result.setRisk(false);
            result.setRiskSummary("");
            return result;
        }

        String safeTitle = StringUtils.isEmpty(title) ? "" : title;
        String safeContent = StringUtils.isEmpty(content) ? "" : content;

        String systemPrompt = "你是企业内部论坛的风控审核助手。请根据标题和内容识别是否存在舆论风险、违法违规风险、攻击诽谤、泄密风险。"
            + "必须只返回JSON，不要输出其他文字。格式为：{\"risk\":true/false,\"riskSummary\":\"...\"}";
        String userPrompt = "请审核以下帖子内容：\\n标题：" + safeTitle + "\\n内容：" + safeContent;

        String raw = invokeChatCompletion(systemPrompt, userPrompt, 0.1);
        String answer = extractAssistantContent(raw);
        String jsonText = tryExtractJsonObject(answer);

        try
        {
            JSONObject obj = JSON.parseObject(jsonText);
            boolean risk = obj.getBooleanValue("risk");
            String riskSummary = obj.getString("riskSummary");
            result.setRisk(risk);
            result.setRiskSummary(StringUtils.isEmpty(riskSummary) ? "AI识别存在潜在风险" : riskSummary);
            result.setRawResponse(answer);
            return result;
        }
        catch (Exception ex)
        {
            log.warn("AI审核结果解析失败，fallback为人工审核。answer={}", answer);
            result.setRisk(true);
            result.setRiskSummary("AI审核结果解析失败，请管理员人工复核");
            result.setRawResponse(answer);
            return result;
        }
    }

    @Override
    public String generatePostContent(String keywords, String postType)
    {
        if (!enabled || StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(model))
        {
            throw new RuntimeException("AI助写未启用，请联系管理员配置豆包参数");
        }
        if (StringUtils.isEmpty(keywords))
        {
            throw new RuntimeException("关键词不能为空");
        }

        String typeDesc = StringUtils.isEmpty(postType) ? "分享" : postType;
        String systemPrompt = "你是企业内部论坛内容助手。请根据用户关键词生成一篇可直接发布的中文帖子内容，结构清晰、语气专业友好。"
            + "禁止输出Markdown代码块标记。";
        String userPrompt = "帖子类型：" + typeDesc + "；关键词：" + keywords
            + "。请根据这些关键词生成一篇完整的帖子内容，要求内容丰富、有条理，适合企业内部论坛发布。";

        String raw = invokeChatCompletion(systemPrompt, userPrompt, 0.7);
        String answer = extractAssistantContent(raw);
        if (StringUtils.isEmpty(answer))
        {
            throw new RuntimeException("AI未返回可用内容，请稍后重试");
        }
        return answer.trim();
    }


    private String invokeChatCompletion(String systemPrompt, String userPrompt, double temperature)
    {
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("temperature", temperature);

        JSONArray messages = new JSONArray();
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        payload.put("messages", messages);
        return doPostJson(apiUrl, payload.toJSONString());
    }

    private String extractAssistantContent(String raw)
    {
        JSONObject obj = JSON.parseObject(raw);
        JSONArray choices = obj.getJSONArray("choices");
        if (choices == null || choices.isEmpty())
        {
            throw new RuntimeException("模型返回为空");
        }
        JSONObject first = choices.getJSONObject(0);
        JSONObject message = first.getJSONObject("message");
        if (message == null)
        {
            throw new RuntimeException("模型返回缺少message");
        }
        return message.getString("content");
    }

    private String tryExtractJsonObject(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return "{}";
        }
        String cleaned = text.trim();
        if (cleaned.startsWith("```"))
        {
            cleaned = cleaned.replace("```json", "").replace("```", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start)
        {
            return cleaned.substring(start, end + 1);
        }
        return cleaned;
    }

    private String doPostJson(String targetUrl, String jsonBody)
    {
        HttpURLConnection connection = null;
        try
        {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            try (OutputStream os = connection.getOutputStream())
            {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int code = connection.getResponseCode();
            InputStream stream = code >= 200 && code < 300
                ? connection.getInputStream()
                : connection.getErrorStream();
            String response = readStream(stream);
            if (code < 200 || code >= 300)
            {
                throw new RuntimeException("调用豆包失败: HTTP " + code + " - " + response);
            }
            return response;
        }
        catch (Exception e)
        {
            throw new RuntimeException("调用豆包失败: " + e.getMessage(), e);
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream inputStream) throws Exception
    {
        if (inputStream == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
