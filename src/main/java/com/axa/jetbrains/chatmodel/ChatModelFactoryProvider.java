package com.axa.jetbrains.chatmodel;

import com.axa.jetbrains.chatmodel.azureopenai.AzureOpenAIChatModelFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ChatModelFactoryProvider {

    private ChatModelFactoryProvider() {
        throw new IllegalStateException("Utility class");
    }

    private static final Map<String, ChatModelFactory> factoryCache = new ConcurrentHashMap<>();

    public static @NotNull Optional<ChatModelFactory> getFactoryByProvider(@NotNull String modelProvider) {
        return Optional.of(factoryCache.computeIfAbsent(modelProvider, ChatModelFactoryProvider::createFactory));
    }

    /**
     * Get the factory by provider.
     *
     * @param modelProvider the model provider
     * @return the factory
     */
    private static @NotNull ChatModelFactory createFactory(@NotNull String modelProvider) {
        return new AzureOpenAIChatModelFactory();
    }
}
