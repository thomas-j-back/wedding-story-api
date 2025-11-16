package com.example.wedding_story_api.provider;

import com.example.wedding_story_api.provider.ImageModelProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProviderRegistry {
    private final Map<String, ImageModelProvider> providers;

    public ProviderRegistry(List<ImageModelProvider> impls) {
        this.providers = impls.stream().collect(Collectors.toMap(
                p -> p.getClass().getSimpleName().replace("Provider","").toLowerCase(), p -> p
        ));
    }
    public ImageModelProvider get(String name) {
        ImageModelProvider p = providers.get(name.toLowerCase());
        if (p == null) throw new IllegalArgumentException("Unknown model provider: " + name);
        return p;
    }
}