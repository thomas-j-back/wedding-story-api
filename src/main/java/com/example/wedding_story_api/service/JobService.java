package com.example.wedding_story_api.service;

import com.example.wedding_story_api.dto.*;
import com.example.wedding_story_api.provider.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JobService {
    private final Map<String,Job> jobs = new ConcurrentHashMap<>();
    private final ProviderRegistry providers; // choose provider by name
    private final StorageService storage;

    public String createJob(CreateJobRequest req) {
        String id = UUID.randomUUID().toString();
        //Creates a new job object
        Job j = new Job(id, req.model(), req.prompt(), req.inputKeys(), "QUEUED", List.of(), null);
        jobs.put(id, j);
        // kick off async work
        CompletableFuture.runAsync(() -> runJob(j, req));
        return id;
    }

    private void runJob(Job j, CreateJobRequest req) {
        j.status = "RUNNING";
        try {
            ImageModelProvider provider = providers.get(req.model());
            // get signed GET URLs for inputs if provider needs HTTP-accessible sources
            List<URI> refs = presignGet(req.inputKeys());
            ImageModelProvider.GenerationResult r =
                    provider.generate(new ImageModelProvider.GenerationRequest(req.prompt(), refs, req.options()));
            if (r.status() != ImageModelProvider.GenerationStatus.SUCCEEDED) {
                j.status = r.status().name();
                j.error  = r.error();
                return;
            }
            // upload provider outputs to S3 (if provider returns bytes) or copy from remote URL
            List<String> keys = uploadResultsToS3(r.outputImages()); // or download+put
            j.outputKeys = keys;

            j.status = "SUCCEEDED";
        } catch (Exception ex) {
            j.status = "FAILED";
            j.error  = ex.getMessage();
        }
    }

    private List<String> uploadResultsToS3(List<URI> uris) {
        return Collections.emptyList();
    }

    public JobStatusDTO status(String id) {
        Job j = jobs.get(id);
        if (j == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        List<String> urls = j.getOutputKeys().stream().map(this::presignGetOnce).toList();
        return new JobStatusDTO(j.getId(), j.getStatus(), urls, j.getError());
    }

    // helpers ...
    private List<URI> presignGet(List<String> keys) { /* .. */ return List.of(); }
    private String presignGetOnce(String key) { /* .. */ return ""; }

    @Data
    @AllArgsConstructor
    static class Job {
        String id; String model; String prompt; List<String> inputKeys;
        String status; List<String> outputKeys; String error;
    }
}
