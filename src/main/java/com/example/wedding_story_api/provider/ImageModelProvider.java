package com.example.wedding_story_api.provider;

import com.example.wedding_story_api.dto.GenerationType;

import java.net.URI;
import java.security.ProviderException;
import java.util.List;
import java.util.Map;

public interface ImageModelProvider {
    GenerationResult generate(GenerationRequest req) throws ProviderException;

    record GenerationRequest(
            String prompt,
            List<URI> referenceImages, // S3 URLs or presigned GET URLs
            Map<String,Object> options, // model-specific knobs
            GenerationType type,
            List<String> inputContentTypes

    ) {}

    record GenerationResult(
            String providerJobId,
            GenerationStatus status,          // QUEUED | RUNNING | SUCCEEDED | FAILED// may be empty until finished
            List<String> outputKeys,
            String error                      // if failed
    ) {}

    enum GenerationStatus { QUEUED, RUNNING, SUCCEEDED, FAILED }


}