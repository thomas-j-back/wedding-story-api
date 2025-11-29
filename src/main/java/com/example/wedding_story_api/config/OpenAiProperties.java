package com.example.wedding_story_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    //    private String apiKey;
    private @Value("${OPEN_API_KEY}") String apiKey;

    private String baseUrl = "https://api.openai.com/v1/responses";

    /**
     * Default model name/id, if you want
     */
    private String model;

    // getters & setters
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}