package com.example.wedding_story_api.dto;

import java.net.URI;
import java.util.List;

public record JobStatusDTO(String jobId, String status, List<String> outputKeys, String error) {}

