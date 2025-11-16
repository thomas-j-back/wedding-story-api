package com.example.wedding_story_api.service;

import com.example.wedding_story_api.dto.PresignedUrlDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final S3Client s3;  // configure via Spring @Bean (region/creds)
    private final S3Presigner presigner;

    @Value("${app.bucket}") String bucket;

    public List<PresignedUrlDTO> createPresignedPutUrls(int count, String contentType, Duration ttl) {
        List<PresignedUrlDTO> out = new ArrayList<>();
        for (int i=0;i<count;i++) {
            String key = "prototype_uploads/%s.jpg".formatted(UUID.randomUUID());
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket).key(key).contentType(contentType).build();
            PresignedPutObjectRequest presigned =
                    presigner.presignPutObject(b -> b.signatureDuration(ttl).putObjectRequest(putReq));
            out.add(new PresignedUrlDTO(presigned.url().toString(), key));
        }
        return out;
    }
}