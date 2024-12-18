package com.axa.jetbrains.service;

import com.axa.jetbrains.error.ErrorHandler;
import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.ui.panel.PromptOutputPanel;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;


public class NonStreamingPromptExecutor {

    private static final Logger LOG = Logger.getInstance(NonStreamingPromptExecutor.class);

    private final PromptExecutionService promptExecutionService;
    private volatile Future<?> currentTask;
    private volatile boolean isCancelled;

    public NonStreamingPromptExecutor() {
        this.promptExecutionService = PromptExecutionService.getInstance();
    }

    /**
     * Execute the prompt.
     *
     * @param chatMessageContext the chat message context
     * @param promptOutputPanel  the prompt output panel
     * @param enableButtons      the enable buttons
     */
    public void execute(ChatMessageContext chatMessageContext,
                        @NotNull PromptOutputPanel promptOutputPanel,
                        Runnable enableButtons) {
        promptOutputPanel.addUserPrompt(chatMessageContext);
        isCancelled = false;

        prompt(chatMessageContext, promptOutputPanel, enableButtons);
    }

    /**
     * Execute the prompt.
     *
     * @param chatMessageContext the chat message context
     * @param promptOutputPanel  the prompt output panel
     * @param enableButtons      the enable buttons
     */
    private void prompt(ChatMessageContext chatMessageContext,
                        @NotNull PromptOutputPanel promptOutputPanel,
                        Runnable enableButtons) {
        currentTask = promptExecutionService.executeQuery(chatMessageContext)
                .thenAccept(response -> {
                    if (!isCancelled && response != null) {
                        LOG.debug(">>>> Adding AI message to prompt output panel");
                        chatMessageContext.setAiMessage(response.content());

                        // Set token usage and cost
                        chatMessageContext.setTokenUsageAndCost(response.tokenUsage());

                        // Add the conversation to the chat service
                        ApplicationManager.getApplication().getMessageBus()
                                .syncPublisher(AppTopics.CONVERSATION_TOPIC)
                                .onNewConversation(chatMessageContext);

                        promptOutputPanel.addChatResponse(chatMessageContext);
                    } else if (isCancelled) {
                        LOG.debug(">>>> Prompt execution cancelled");
                        promptOutputPanel.removeLastUserPrompt(chatMessageContext);
                    }
                })
                .exceptionally(throwable -> {
                    if (!(throwable.getCause() instanceof CancellationException)) {
                        LOG.error("Error occurred while processing chat message", throwable);
                        ErrorHandler.handleError(chatMessageContext.getProject(), throwable);
                    }
                    return null;
                })
                .whenComplete((result, throwable) -> enableButtons.run());
    }


    /**
     * Stop prompt execution.
     */
    public void stopExecution() {
        if (currentTask != null && !currentTask.isDone()) {
            isCancelled = true;
            currentTask.cancel(true);
        }
    }
}
