package com.axa.jetbrains.chatmodel;

import com.axa.jetbrains.model.ChatModel;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.service.LLMModelRegistryService;
import com.axa.jetbrains.service.LLMProviderService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;

import java.util.List;

public interface ChatModelFactory {

    String TEST_MODEL = "test-model";

    /**
     * Create a chat model with the given parameters.
     *
     * @param chatModel the chat model
     * @return the chat model
     */
    ChatLanguageModel createChatModel(ChatModel chatModel);

    /**
     * Create a streaming chat model with the given parameters.
     *
     * @param chatModel the chat model
     * @return the streaming chat model
     */
    default StreamingChatLanguageModel createStreamingChatModel(ChatModel chatModel) {
        return null;
    }

    /**
     * Get available models for selected provider
     *
     * @return the list of models
     */
    default List<LanguageModel> getModels(ModelProvider provider) {
        return LLMModelRegistryService.getInstance().getModels()
            .stream()
            .filter(model -> model.getProvider().equals(provider))
            .toList();
    }

    /**
     * Get available models for selected provider
     *
     * @return the list of models
     */
    List<LanguageModel> getModels();

    default String getApiKey(ModelProvider modelProvider) {
        return LLMProviderService.getInstance().getApiKey(modelProvider).trim();
    }

    /**
     * Reset the list of local models
     */
    default void resetModels() {}
}
