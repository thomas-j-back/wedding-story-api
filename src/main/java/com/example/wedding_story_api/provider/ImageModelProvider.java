package com.example.wedding_story_api.provider;

import java.net.URI;
import java.security.ProviderException;
import java.util.List;
import java.util.Map;

public interface ImageModelProvider {
    GenerationResult generate(GenerationRequest req) throws ProviderException;

    record GenerationRequest(
            String prompt,
            List<URI> referenceImages, // S3 URLs or presigned GET URLs
            Map<String,Object> options // model-specific knobs
    ) {}

    record GenerationResult(
            String providerJobId,
            GenerationStatus status,          // QUEUED | RUNNING | SUCCEEDED | FAILED
            List<URI> outputImages,           // may be empty until finished
            String error                      // if failed
    ) {}

    enum GenerationStatus { QUEUED, RUNNING, SUCCEEDED, FAILED }
}