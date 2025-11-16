package com.example.wedding_story_api.beans;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@Configuration
public class S3Config {

   public S3Client s3Client(
           @Value("${r2.access-key}") String accessKey,
           @Value("${r2.secret-key}") String secretKey,
           @Value("${r2.endpoint}") String endpoint
   ) {

       return S3Client.builder().region(Region.of("auto"))
               .endpointOverride(URI.create(endpoint))
               .credentialsProvider(StaticCredentialsProvider.create(
                       AwsBasicCredentials.create(accessKey,secretKey)
               ))
               .build();
   }
}
