package com.devoxx.genie.chatmodel;

import com.devoxx.genie.chatmodel.cloud.azureopenai.AzureOpenAIChatModelFactory;
import com.devoxx.genie.chatmodel.local.ollama.OllamaChatModelFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ChatModelFactoryProvider {

    private ChatModelFactoryProvider() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<String, ChatModelFactory> factoryCache = new ConcurrentHashMap<>();

    public static @NotNull Optional<ChatModelFactory> getFactoryByProvider(@NotNull String modelProvider) {
        return Optional.ofNullable(factoryCache.computeIfAbsent(modelProvider, ChatModelFactoryProvider::createFactory));
    }

    /**
     * Get the factory by provider.
     *
     * @param modelProvider the model provider
     * @return the factory
     */
    private static @Nullable ChatModelFactory createFactory(@NotNull String modelProvider) {
        return switch (modelProvider) {
            case "Axa Secure GPT" -> new AzureOpenAIChatModelFactory();
            case "Ollama" -> new OllamaChatModelFactory();
            default -> null;
        };
    }
}
