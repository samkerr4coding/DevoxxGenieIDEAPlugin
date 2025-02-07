package com.devoxx.genie.model.enums;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ModelProvider {
    Ollama("Ollama", Type.LOCAL),
    AzureOpenAI("Axa Secure GPT", Type.OPTIONAL);

    public enum Type {
        LOCAL, // Local Providers
        CLOUD, // Cloud Providers
        OPTIONAL // Optional Providers(Need to be enabled from settings, due to inconvenient setup)
    }

    private final String name;
    private final Type type;

    ModelProvider(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return name;
    }

    public static @NotNull ModelProvider fromString(String text) {
        for (ModelProvider provider : ModelProvider.values()) {
            if (provider.name.equalsIgnoreCase(text)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }

    public static List<ModelProvider> fromType(ModelProvider.Type type) {
        return Arrays.stream(ModelProvider.values())
                .filter(provider -> provider.type == type)
                .toList();
    }
}
