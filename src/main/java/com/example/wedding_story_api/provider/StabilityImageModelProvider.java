package com.example.wedding_story_api.provider;

import com.example.wedding_story_api.config.StabilityProperties;
import com.example.wedding_story_api.dto.PresignedUrlDTO;
import com.example.wedding_story_api.service.StorageService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.ProviderException;
import java.time.Duration;
import java.util.*;

import static com.example.wedding_story_api.dto.GenerationType.*;

@Component
public class StabilityImageModelProvider implements ImageModelProvider {

    private final WebClient webClient;
    private final StabilityProperties properties;
    private final StorageService storageService; // your own service for R2/S3
    private final WebClient.Builder webClientBuilder;
    private final MediaType OUTPUT_MEDIA_TYPE = MediaType.IMAGE_PNG;

    public StabilityImageModelProvider(
            StabilityProperties properties,
            WebClient.Builder webClientBuilder,
            StorageService storageService
    ) {
        this.properties = properties;
        this.storageService = storageService;
        this.webClientBuilder = webClientBuilder;

        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("accept", "image/*")
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) // 16 MB
                )
                .build();
    }

    @Override
    public GenerationResult generate(GenerationRequest req) throws ProviderException {
        //First get the presigned image data
        return switch(req.type()) {
            case STYLE_TRANSFER -> handleStyleTransfer(req);
            case TEXT_TO_IMAGE -> handleTxt2Img(req);

            default -> new GenerationResult(
                    UUID.randomUUID().toString(),
                    GenerationStatus.FAILED,
                    List.of(),
                    "Unsupported generation type: " + req.type()
            );
        };


    }

    private GenerationResult handleTxt2Img(GenerationRequest req) {
        return new GenerationResult(
                UUID.randomUUID().toString(),
                GenerationStatus.FAILED,
                List.of(),
                "This generationType is not implemented yet"
        );
    }

    private GenerationResult handleStyleTransfer(GenerationRequest req) {
        try {
        if (req.referenceImages() == null || req.referenceImages().size() < 2) {
            return new GenerationResult(
                    UUID.randomUUID().toString(),
                    GenerationStatus.FAILED,
                    List.of(),
                    "Style transfer requires at least 2 reference images (content + style)"
            );
        }
        //0 will be init_image, 1 will be style_image
        List<byte[]> images = new ArrayList<>();
        for (URI ref : req.referenceImages()) {
            byte[] bytes = storageService.getFromPresignedUrl(ref);
            images.add(bytes);
        }

        var builder = new org.springframework.http.client.MultipartBodyBuilder();

        builder.part("init_image", images.get(0))
                .filename("init_image" + this.getSuffixFromMediaType(MediaType.parseMediaType(req.inputContentTypes().getFirst())))
                .contentType(MediaType.parseMediaType(req.inputContentTypes().getFirst()));

        builder.part("style_image", images.get(1))
                .filename("style_image" + this.getSuffixFromMediaType(MediaType.parseMediaType(req.inputContentTypes().get(1))))
                .contentType(MediaType.parseMediaType(req.inputContentTypes().get(1)));

        //For now we want to just add a custom prompt, not from front end, so we can generate a character
//        if (req.prompt() != null && !req.prompt().isBlank()) {
//            builder.part("prompt", req.prompt());
//        }
        builder.part("style_strength", "0.95");
        builder.part("prompt", "recreate the person in the photo as a simple character using the same exact hand drawn style of the people in the style image. Only use solid colors that exist in the style image");
        builder.part("negative_prompt", "do not keep realistic details or a detailed background, background should be one color from the style image");
        builder.part("output_format", "png");

       byte[] generationResBytes = webClient.post()
               .uri("/v2beta/stable-image/control/style-transfer")
               .contentType(MediaType.MULTIPART_FORM_DATA)
               .accept(MediaType.valueOf("image/*"))
               .bodyValue(builder.build())
               .retrieve()
               .bodyToMono(byte[].class)
               .block();

       if (generationResBytes == null || generationResBytes.length == 0) {
            return new GenerationResult(
                    UUID.randomUUID().toString(),
                    GenerationStatus.FAILED,
                    List.of(),
                    "Empty image from Stability style transfer"
            );
        }
        String generatedKey = "character_outputs/" + UUID.randomUUID().toString() + ".png";
        storeResult(generationResBytes, this.OUTPUT_MEDIA_TYPE, generatedKey);
//
        return new GenerationResult(
                UUID.randomUUID().toString(),
                GenerationStatus.SUCCEEDED,
                List.of(generatedKey),
                null
        );


    } catch (RuntimeException e) {
        return new GenerationResult(
                UUID.randomUUID().toString(),
                GenerationStatus.FAILED,
                List.of(),
                "Runtime Exception error occurred on Stability API call: " + e.getMessage()
        );
        }
    }

    private URI storeResult(byte[] result, MediaType contentType, String someKey){
        return storageService.uploadImageToBucket(result, contentType, someKey);
    }

    /**
     * Translate your GenerationRequest into whatever JSON Stability expects.
     * You can expand this to support more options later.
     */
    private Map<String, Object> buildStabilityRequestBody(GenerationRequest req) {
        // Base fields
        var body = new HashMap<String, Object>();

        body.put("prompt", req.prompt());

        // Example: allow overriding model-specific options via req.options()
        if (req.options() != null) {
            body.putAll(req.options());
        }

        // Example: pass reference images as URLs if the model supports it
        if (req.referenceImages() != null && !req.referenceImages().isEmpty()) {
            List<String> refs = req.referenceImages().stream()
                    .map(URI::toString)
                    .toList();
            body.put("reference_images", refs);
        }

        return body;
    }

    private String getSuffixFromMediaType(MediaType type) {

        if(type.toString().contains("jpg") || type.toString().contains("jpeg")) {
            return".jpg";
        }
        if(type.toString().contains("png"))
            return ".png";
        return "";
    }

    /**
     * Some simple storage abstraction – you’ll implement this against R2 or S3.
     */
    public interface ImageStorageService {
        URI storeGeneratedImage(InputStream imageStream, String contentType) throws Exception;
    }

}
