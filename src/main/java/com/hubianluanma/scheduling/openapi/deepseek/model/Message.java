package com.hubianluanma.scheduling.openapi.deepseek.model;

/**
 * @author huhailong
 * @version 1.0
 * @description: TODO
 * @date 2025/7/31 12:54
 */
public class Message {

    private String role;
    private String content;

    public Message() {
    }

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
