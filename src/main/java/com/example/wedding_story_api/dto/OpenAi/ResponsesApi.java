package com.example.wedding_story_api.dto.OpenAi;

import java.util.List;
import java.util.Map;

public class ResponsesApi {

    public record ResponseOutput<ImageData>(
            String type,          // "output_text", "tool_call", "output_image", etc.
            String role,          // might be null for tool calls
            List<ContentItem> content,
            Map<String, Object> parameters,  // tool_call params
            ImageData image

    ) {}

    public record ResponsesApiResponse(
            String id,
            String model,
            List<ResponseOutput> output

    ) {}

    public record ContentItem(
            String type,
            String text
    ) {}

    public record ImageData(
            String format,
            String data,
            String url
    ) {}
}
