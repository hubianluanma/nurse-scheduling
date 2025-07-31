package com.hubianluanma.scheduling.openapi.deepseek.model;

import java.util.List;
import java.util.Map;

/**
 * @author huhailong
 * @version 1.0
 * @description: TODO
 * @date 2025/7/31 12:55
 */
public class ChatRequest {

    private String model;
    private List<Message> messages;
    private Map<String,String> response_format;
    private boolean stream;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Map<String,String> getResponse_format() {
        return response_format;
    }

    public void setResponse_format(Map<String,String> response_format) {
        this.response_format = response_format;
    }

    public boolean isStream() {
        return stream;
    }

    public void setStream(boolean stream) {
        this.stream = stream;
    }
}
