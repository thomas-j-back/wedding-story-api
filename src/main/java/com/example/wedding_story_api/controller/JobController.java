package com.example.wedding_story_api.controller;

import com.example.wedding_story_api.dto.*;
import com.example.wedding_story_api.service.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.time.Duration;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobs;
    private final StorageService storage;

    @PostMapping("/upload-urls")
    public List<PresignedUrlDTO> createUploadUrls(@RequestBody CreateUploadDTO body) {
//        System.out.println(">>> Controller auth = " + authentication);

        return storage.createPresignedPutUrls(body.count(), body.contentType(), Duration.ofMinutes(5));
    }

    @PostMapping("/jobs")
    public CreateJobResponse createJob(@Valid @RequestBody CreateJobRequest req) {
        String jobId = jobs.createJob(req);
        return new CreateJobResponse(jobId);
    }

    @GetMapping("/jobs/{id}")
    public JobStatusDTO get(@PathVariable String id) {
        return jobs.status(id);
    }
}