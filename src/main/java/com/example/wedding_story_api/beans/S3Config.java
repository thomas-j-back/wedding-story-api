package com.example.wedding_story_api.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@Configuration
public class S3Config {

   public S3Client s3Client() {
       String accountId = System.getenv("R2_ACCOUNT_ID");
       String accessKey = System.getenv("R2_ACCESS_KEY_ID");
       String secretKey = System.getenv("R2_SECRET_ACESS_KEY");

       String apiEndpoint = "https://" + accountId + ".r2.cloudflarestorage.com";

       return S3Client.builder().region(Region.of("auto"))
               .endpointOverride(URI.create(apiEndpoint))
               .credentialsProvider(StaticCredentialsProvider.create(
                       AwsBasicCredentials.create(accessKey,secretKey)
               ))
               .build();
   }
}
