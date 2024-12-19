package com.axa.jetbrains.chatmodel;

import com.axa.jetbrains.model.ChatModel;
import com.axa.jetbrains.model.Constant;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.service.AxaAiSettingsService;
import com.axa.jetbrains.ui.settings.AxaAiStateService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.axa.jetbrains.chatmodel.ChatModelFactory.TEST_MODEL;

@Setter
public class ChatModelProvider {

    private static final ModelProvider DEFAULT_PROVIDER = ModelProvider.OpenAI; // Choose an appropriate default

    public ChatLanguageModel getChatLanguageModel(@NotNull ChatMessageContext chatMessageContext) {
        ChatModel chatModel = initChatModel(chatMessageContext);
        ChatModelFactory factory = getFactory(chatMessageContext);

        return factory.createChatModel(chatModel);
    }

    public StreamingChatLanguageModel getStreamingChatLanguageModel(@NotNull ChatMessageContext chatMessageContext) {
        ChatModel chatModel = initChatModel(chatMessageContext);
        return getFactory(chatMessageContext).createStreamingChatModel(chatModel);
    }

    private @NotNull ChatModelFactory getFactory(@NotNull ChatMessageContext chatMessageContext) {
        ModelProvider provider = Optional.ofNullable(chatMessageContext.getLanguageModel())
            .map(LanguageModel::getProvider)
            .orElse(DEFAULT_PROVIDER);

        return ChatModelFactoryProvider.getFactoryByProvider(provider.name())
            .orElseThrow(() -> new IllegalArgumentException("No factory for provider: " + provider));
    }

    public @NotNull ChatModel initChatModel(@NotNull ChatMessageContext chatMessageContext) {
        ChatModel chatModel = new ChatModel();
        AxaAiSettingsService stateService = AxaAiStateService.getInstance();
        setMaxOutputTokens(stateService, chatModel);

        chatModel.setTemperature(stateService.getTemperature());
        chatModel.setMaxRetries(stateService.getMaxRetries());
        chatModel.setTopP(stateService.getTopP());
        chatModel.setTimeout(stateService.getTimeout());

        LanguageModel languageModel = chatMessageContext.getLanguageModel();
        chatModel.setModelName(languageModel.getModelName() == null ? TEST_MODEL : languageModel.getModelName());

        return chatModel;
    }

    private static void setMaxOutputTokens(@NotNull AxaAiSettingsService settingsState,
                                           @NotNull ChatModel chatModel) {
        Integer maxOutputTokens = settingsState.getMaxOutputTokens();
        chatModel.setMaxTokens(maxOutputTokens != null ? maxOutputTokens : Constant.MAX_OUTPUT_TOKENS);
    }
}
