package com.example.wedding_story_api.dto;

import java.util.List;

public record JobStatusDTO(String jobId, String status, List<String> resultUrls, String error) {}

