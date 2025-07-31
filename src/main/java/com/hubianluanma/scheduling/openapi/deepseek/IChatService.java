package com.hubianluanma.scheduling.openapi.deepseek;

/**
 * @author huhailong
 * @version 1.0
 * @description: deepseek聊天服务接口
 * @date 2025/7/31 12:41
 */
public interface IChatService {

    String convertMarkdownToJson(String jsonRule, String markdownContent);

    String getResponseContent(String responseJson);
}
