package com.example.demo.error;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "errors")
public class ErrorMessagesProperties {

    private Map<String, String> messages;

    public Map<String, String> getMessages() {
        return messages;
    }

    public void setMessages(Map<String, String> messages) {
        this.messages = messages;
    }

    public String get(String key) {
        return messages.getOrDefault(key, "خطای ناشناخته");
    }
}
