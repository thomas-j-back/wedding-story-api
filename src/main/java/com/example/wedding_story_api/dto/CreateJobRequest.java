package com.example.wedding_story_api.dto;

import java.util.List;
import java.util.Map;

public record CreateJobRequest(
        String model,
        String prompt,
        List<String> inputKeys, // S3 object keys from /upload-urls step
        Map<String,Object> options
) {}
