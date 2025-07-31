package com.hubianluanma.scheduling.openapi.deepseek.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubianluanma.scheduling.openapi.deepseek.IChatService;
import com.hubianluanma.scheduling.openapi.deepseek.model.ChatRequest;
import com.hubianluanma.scheduling.openapi.deepseek.model.Message;
import com.hubianluanma.scheduling.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author huhailong
 * @version 1.0
 * @description: TODO
 * @date 2025/7/31 12:42
 */
@Service
public class ChatServiceImpl implements IChatService {

    @Value("${deepseek.apiKey}")
    private String apiKey;
    @Value("${deepseek.baseUrl}")
    private String baseUrl;
    @Value("${deepseek.model}")
    private String model;

    @Autowired
    HttpUtil httpUtil;

    @Override
    public String convertMarkdownToJson(String jsonRule, String markdownContent) {
        String systemPrompt = """
            接下来用户将提供一段Markdown格式的文本内容，主要内容是一个表格，里面包含了不同人员的排班信息和其他相关信息，请你根据提供的Markdown解析内容并生成JSON格式的结果，json结构需要严格按照下面给出的json格式返回： 
            
            EXAMPLE JSON OUTPUT:
        """ + jsonRule;
        String userPrompt = "Please parse the following text: " + markdownContent;
        List<Message> messages = List.of(
                new Message("system", systemPrompt),
                new Message("user", userPrompt)
        );
        return chatForAI(messages);
    }

    @Override
    public String getResponseContent(String responseJson) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(responseJson);
            return jsonNode.withArray("choices").get(0).get("message").get("content").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String chatForAI(List<Message> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setModel(model);
        chatRequest.setMessages(messages);
        chatRequest.setResponse_format(Map.of("type", "json_object"));
        chatRequest.setStream(false);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, headers);
        return httpUtil.post(baseUrl+"/chat/completions", entity, String.class);
    }
}
