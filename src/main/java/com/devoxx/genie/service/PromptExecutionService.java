package com.devoxx.genie.service;

import com.devoxx.genie.error.ErrorHandler;
import com.devoxx.genie.model.Constant;
import com.devoxx.genie.model.request.ChatMessageContext;
import com.devoxx.genie.service.exception.ProviderUnavailableException;
import com.devoxx.genie.ui.settings.DevoxxGenieStateService;
import com.devoxx.genie.util.ChatMessageContextUtil;
import com.devoxx.genie.util.ClipboardUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.bedrock.BedrockMistralAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class PromptExecutionService {

    private static final Logger LOG = Logger.getInstance(PromptExecutionService.class);
    private final ExecutorService queryExecutor = Executors.newSingleThreadExecutor();
    private CompletableFuture<ChatResponse> queryFuture = null;

    @Getter
    private boolean running = false;

    private final ReentrantLock queryLock = new ReentrantLock();

    @NotNull
    static PromptExecutionService getInstance() {
        return ApplicationManager.getApplication().getService(PromptExecutionService.class);
    }

    /**
     * Execute the query with the given language text pair and chat language model.
     *
     * @param chatMessageContext the chat message context
     * @return the response
     */
    public @NotNull CompletableFuture<ChatResponse> executeQuery(@NotNull ChatMessageContext chatMessageContext) {
        LOG.debug("Execute query : " + chatMessageContext);

        queryLock.lock();
        try {
            if (isCanceled()) return CompletableFuture.completedFuture(null);

            ChatMemoryService chatMemoryService = ChatMemoryService.getInstance();

            // Add System Message if ChatMemoryService is empty
            if (ChatMemoryService.getInstance().isEmpty(chatMessageContext.getProject())) {
                LOG.debug("ChatMemoryService is empty, adding a new SystemMessage");

                if (includeSystemMessage(chatMessageContext)) {
                    String systemPrompt = DevoxxGenieStateService.getInstance().getSystemPrompt() + Constant.MARKDOWN;
                    chatMemoryService.add(chatMessageContext.getProject(), SystemMessage.from(systemPrompt));
                }
            }

            // Add User message to context
            MessageCreationService.getInstance().addUserMessageToContext(chatMessageContext);
            chatMemoryService.add(chatMessageContext.getProject(), chatMessageContext.getUserMessage());

            long startTime = System.currentTimeMillis();

            queryFuture = CompletableFuture
                .supplyAsync(() -> processChatMessage(chatMessageContext), queryExecutor)
                .orTimeout(
                    chatMessageContext.getTimeout() == null ? 60 : chatMessageContext.getTimeout(), TimeUnit.SECONDS)
                .thenApply(result -> {
                    chatMessageContext.setExecutionTimeMs(System.currentTimeMillis() - startTime);
                    return result;
                })
                .exceptionally(throwable -> {
                    LOG.error("Error occurred while processing chat message", throwable);
                    ErrorHandler.handleError(chatMessageContext.getProject(), throwable);
                    return null;
                });
        } finally {
            queryLock.unlock();
        }
        return queryFuture;
    }

    private boolean includeSystemMessage(@NotNull ChatMessageContext chatMessageContext) {
        // If the language model is OpenAI o1 model, do not include system message
        if (ChatMessageContextUtil.isOpenAIo1Model(chatMessageContext.getLanguageModel())) {
            return false;
        }

        // If Bedrock Mistral AI model is selected, do not include system message
        if (chatMessageContext.getChatLanguageModel() instanceof BedrockMistralAiChatModel bedrockMistralAiChatModel) {
            return bedrockMistralAiChatModel.getModel().startsWith("mistral.");
        }

        return true;
    }

    /**
     * If the future task is not null this means we need to cancel it
     *
     * @return true if the task is canceled
     */
    private boolean isCanceled() {
        if (queryFuture != null && !queryFuture.isDone()) {
            queryFuture.cancel(true);
            running = false;
            return true;
        }
        running = true;
        return false;
    }

    /**
     * Process the chat message.
     *
     * @param chatMessageContext the chat message context
     * @return the AI response
     */
    private @NotNull ChatResponse processChatMessage(ChatMessageContext chatMessageContext) {
        try {
            ChatLanguageModel chatLanguageModel = chatMessageContext.getChatLanguageModel();
            List<ChatMessage> messages = ChatMemoryService.getInstance().messages(chatMessageContext.getProject());

            ClipboardUtil.copyToClipboard(messages.toString());

            ChatResponse chatResponse = chatLanguageModel.chat(messages);
            ChatMemoryService.getInstance().add(chatMessageContext.getProject(), chatResponse.aiMessage());

            return chatResponse;

        } catch (Exception e) {

            ChatMemoryService.getInstance().removeLast(chatMessageContext.getProject());
            throw new ProviderUnavailableException(e.getMessage());
        }
    }
}
