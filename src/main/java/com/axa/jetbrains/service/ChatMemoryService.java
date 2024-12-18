package com.axa.jetbrains.service;

import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.conversation.Conversation;
import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.ui.listener.ChatMemorySizeListener;
import com.axa.jetbrains.ui.settings.DevoxxGenieStateService;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.axa.jetbrains.util.ChatMessageContextUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatMemoryService implements ChatMemorySizeListener {

    private final Map<String, MessageWindowChatMemory> projectMemories = new ConcurrentHashMap<>();
    private final InMemoryChatMemoryStore inMemoryChatMemoryStore = new InMemoryChatMemoryStore();
    private LanguageModel currentLanguageModel;

    public static ChatMemoryService getInstance() {
        return ApplicationManager.getApplication().getService(ChatMemoryService.class);
    }

    public void init(@NotNull Project project) {
        createChatMemory(project.getLocationHash(), DevoxxGenieStateService.getInstance().getChatMemorySize());
        createChangeListener();
    }

    private void createChangeListener() {
        ApplicationManager.getApplication().getMessageBus()
            .connect()
            .subscribe(AppTopics.CHAT_MEMORY_SIZE_TOPIC, this);
    }

    public void clear(@NotNull Project project) {
        projectMemories.get(project.getLocationHash()).clear();
    }

    public void add(@NotNull Project project, ChatMessage chatMessage) {
        if (chatMessage instanceof SystemMessage && ChatMessageContextUtil.isOpenAIo1Model(currentLanguageModel)) {
            return;
        }
        projectMemories.get(project.getLocationHash()).add(chatMessage);
    }

    public void remove(@NotNull ChatMessageContext chatMessageContext) {
        currentLanguageModel = chatMessageContext.getLanguageModel();

        Project project = chatMessageContext.getProject();
        List<ChatMessage> messages = projectMemories.get(project.getLocationHash()).messages();
        messages.remove(chatMessageContext.getAiMessage());
        messages.remove(chatMessageContext.getUserMessage());

        // Remove the conversation from the storage service
        projectMemories.get(project.getLocationHash()).clear();
        this.currentLanguageModel = chatMessageContext.getLanguageModel();
        messages.forEach(message -> add(project, message));
    }

    public void removeLast(@NotNull Project project) {
        List<ChatMessage> messages = projectMemories.get(project.getLocationHash()).messages();
        if (!messages.isEmpty()) {
            messages.remove(messages.size() - 1);
            projectMemories.get(project.getLocationHash()).clear();
            messages.forEach(message -> add(project, message));
        }
    }

    public List<ChatMessage> messages(@NotNull Project project) {
        return projectMemories.get(project.getLocationHash()).messages();
    }

    public boolean isEmpty(@NotNull Project project) {
        return projectMemories.get(project.getLocationHash()).messages().isEmpty();
    }

    @Override
    public void onChatMemorySizeChanged(int chatMemorySize) {
        projectMemories.forEach((project, memory) -> createChatMemory(project, chatMemorySize));
    }

    private void createChatMemory(@NotNull String projectHash, int chatMemorySize) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
            .id("axa-ai-jetbrains-plugin-" + projectHash)
            .chatMemoryStore(inMemoryChatMemoryStore)
            .maxMessages(chatMemorySize)
            .build();
        projectMemories.put(projectHash, chatMemory);
    }

    public void restoreConversation(@NotNull Project project, @NotNull Conversation conversation) {
        clear(project);
        for (com.axa.jetbrains.model.conversation.ChatMessage message : conversation.getMessages()) {
            if (message.isUser()) {
                add(project, new UserMessage(message.getContent()));
            } else {
                add(project, new AiMessage(message.getContent()));
            }
        }
    }
}
