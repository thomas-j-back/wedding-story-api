package com.example.wedding_story_api.provider;

import com.example.wedding_story_api.config.OpenAiProperties;
import com.example.wedding_story_api.config.StabilityProperties;
import com.example.wedding_story_api.dto.CreateJobRequest;
import com.example.wedding_story_api.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.security.ProviderException;
import java.util.List;
import java.util.UUID;

public class OpenAiImageModelProvider implements ImageModelProvider{

    private final WebClient webClient;
    private final OpenAiProperties properties;
    private final StorageService storageService; // your own service for R2/S3
    private final WebClient.Builder webClientBuilder;
    private final MediaType OUTPUT_MEDIA_TYPE = MediaType.IMAGE_PNG;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAiImageModelProvider(
            OpenAiProperties properties,
            WebClient.Builder webClientBuilder,
            StorageService storageService
    ) {
        this.properties = properties;
        this.storageService = storageService;
        this.webClientBuilder = webClientBuilder;

        this.webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + this.properties.getApiKey())
                .defaultHeader("Content-Type","application/json")
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
//            case TEXT_TO_IMAGE -> handleTxt2Img(req);

            default -> new GenerationResult(
                    UUID.randomUUID().toString(),
                    GenerationStatus.FAILED,
                    List.of(),
                    "Unsupported generation type: " + req.type()
            );
        };

    }

    public GenerationResult handleStyleTransfer(GenerationRequest req) {
        if (req.referenceImages() == null || req.referenceImages().size() < 2) {
            return new GenerationResult(
                    UUID.randomUUID().toString(),
                    GenerationStatus.FAILED,
                    List.of(),
                    "Style transfer requires at least 2 reference images (content + style)"
            );
        }


        webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createStyleTransferRequestBody(
                        "gtp-5",
                        req.referenceImages().getFirst(),
                        req.referenceImages().get(1),
                        "")
                )
                .retrieve()
                .bodyToMono(Void.class);

        /**
         * {
         *   "type": "tool_call",
         *   "tool_name": "image_generation",
         *   "parameters": { ... }
         * },
         * {
         *   "type": "output_image",
         *   "image": {
         *     "data": "base64...",
         *     "format": "png"
         *   }
         * }
         */

    }

    public ObjectNode createStyleTransferRequestBody(String model, URI styleImageGetUri, URI inputImageGetUri, String prompt) {

        ObjectNode inputText = _createContentObj("input_text", "The FIRST image shows the CHARACTERS I care about. The SECOND image shows the STYLE I want. Generate a new image that keeps the characters, poses, and composition of the FIRST image, but redraws them in the art style, colors, and line quality of the SECOND image.");

        ObjectNode inputImage =  _createContentObj("input_image", inputImageGetUri.toString());

        ObjectNode styleImage = _createContentObj("input_image", styleImageGetUri.toString());

        ArrayNode contentArray = mapper.createArrayNode()
                .add(inputText)
                .add(inputImage)
                .add(styleImage);
        ObjectNode inputObj = mapper.createObjectNode();
        inputObj.put("content", contentArray);
        inputObj.put("role", "user");

        ArrayNode inputArray = mapper.createArrayNode();
        inputArray.add(inputObj);

        ObjectNode body = mapper.createObjectNode();
        body.put("input", inputArray);
        body.put("model", model);

        body.put("tools" ,_createToolsOptions());

        return body;
    }

    private ArrayNode _createToolsOptions() {
        /**
         *  "tools": [
         *     {
         *       "type": "image_generation",
         *       "model": "gpt-image-1",
         *       "size": "1024x1024",
         *       "quality": "high",
         *       "background": "transparent"
         *     }
         *   ],
         */


        ArrayNode toolsArr = mapper.createArrayNode();
        ObjectNode toolsInArr = mapper.createObjectNode();

        toolsInArr.put("type", "image_generation");
        toolsInArr.put("model", "gpt-image-1");
        toolsInArr.put("size", "720x720");
        toolsInArr.put("quality","high");

        toolsArr.add(toolsInArr);
//        tools.put("tools", toolsArr);
        return toolsArr;


    }

    private ObjectNode _createContentObj(String type, String content, ) {
        ObjectNode input = mapper.createObjectNode();
        input.put("type", type);
        String contentKey = "text";
        if(type == "input_image") {
            contentKey = "image_url";
        }
        input.put(contentKey, content);
        return input;
    }
}


