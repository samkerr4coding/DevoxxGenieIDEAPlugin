package com.axa.jetbrains.service.streaming;

import com.axa.jetbrains.error.ErrorHandler;
import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.service.ChatMemoryService;
import com.axa.jetbrains.service.FileListManager;
import com.axa.jetbrains.ui.component.ExpandablePanel;
import com.axa.jetbrains.ui.panel.ChatStreamingResponsePanel;
import com.axa.jetbrains.ui.panel.PromptOutputPanel;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import org.jetbrains.annotations.NotNull;

public class StreamingResponseHandler implements dev.langchain4j.model.StreamingResponseHandler<AiMessage> {
    private final ChatMessageContext chatMessageContext;
    private final Runnable enableButtons;
    private final ChatStreamingResponsePanel streamingChatResponsePanel;
    private final PromptOutputPanel promptOutputPanel;
    private volatile boolean isStopped = false;
    private final long startTime;
    private final StringBuilder responseBuilder = new StringBuilder();

    public StreamingResponseHandler(ChatMessageContext chatMessageContext,
                                    @NotNull PromptOutputPanel promptOutputPanel,
                                    Runnable enableButtons) {
        this.chatMessageContext = chatMessageContext;
        this.enableButtons = enableButtons;
        this.promptOutputPanel = promptOutputPanel;
        this.streamingChatResponsePanel = new ChatStreamingResponsePanel(chatMessageContext);
        promptOutputPanel.addStreamResponse(streamingChatResponsePanel);
        startTime = System.currentTimeMillis();
    }

    @Override
    public void onNext(String token) {
        if (!isStopped) {
            streamingChatResponsePanel.insertToken(token);
        }
    }

    @Override
    public void onComplete(@NotNull Response<AiMessage> response) {
        if (isStopped) {
            return;
        }
        long endTime = System.currentTimeMillis();
        chatMessageContext.setExecutionTimeMs(endTime - startTime);
        finalizeResponse(response);
        addExpandablePanelIfNeeded();
    }

    private void finalizeResponse(@NotNull Response<AiMessage> response) {

        chatMessageContext.setAiMessage(response.content());

        ApplicationManager.getApplication().getMessageBus()
            .syncPublisher(AppTopics.CONVERSATION_TOPIC)
            .onNewConversation(chatMessageContext);

        ChatMemoryService.getInstance().add(chatMessageContext.getProject(), response.content());
        enableButtons.run();
    }

    private void addExpandablePanelIfNeeded() {
        if (chatMessageContext.hasFiles()) {
            ApplicationManager.getApplication().invokeLater(() -> {
                ExpandablePanel fileListPanel =
                    new ExpandablePanel(chatMessageContext, FileListManager.getInstance().getFiles());
                fileListPanel.setName(chatMessageContext.getId());
                promptOutputPanel.addStreamFileReferencesResponse(fileListPanel);
            });
        }
    }

    public void stop() {
        isStopped = true;
        enableButtons.run();
    }

    @Override
    public void onError(Throwable error) {
        enableButtons.run();
        ErrorHandler.handleError(chatMessageContext.getProject(), error);
    }
}
