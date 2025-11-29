package com.example.wedding_story_api.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


public record CreateJobRequest(
                                String model,
                                String prompt,
                                List<String> inputKeys, // S3 object keys from /upload-urls step
                                Map<String,Object> options,
                                List<String> inputContentTypes,
                                GenerationType type){}
