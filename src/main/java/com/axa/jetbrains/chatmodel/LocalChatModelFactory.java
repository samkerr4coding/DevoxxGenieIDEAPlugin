package com.axa.jetbrains.chatmodel;

import com.axa.jetbrains.model.ChatModel;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;
import com.axa.jetbrains.ui.util.NotificationUtil;
import com.intellij.openapi.project.ProjectManager;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import dev.langchain4j.model.localai.LocalAiStreamingChatModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class LocalChatModelFactory implements ChatModelFactory {

    protected final ModelProvider modelProvider;
    protected List<LanguageModel> cachedModels = null;
    protected static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    protected static boolean warningShown = false;

    protected LocalChatModelFactory(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    @Override
    public abstract ChatLanguageModel createChatModel(@NotNull ChatModel chatModel);

    @Override
    public abstract StreamingChatLanguageModel createStreamingChatModel(@NotNull ChatModel chatModel);

    protected abstract String getModelUrl();

    protected ChatLanguageModel createLocalAiChatModel(@NotNull ChatModel chatModel) {
        return LocalAiChatModel.builder()
                .baseUrl(getModelUrl())
                .modelName(chatModel.getModelName())
                .maxRetries(chatModel.getMaxRetries())
                .temperature(chatModel.getTemperature())
                .maxTokens(chatModel.getMaxTokens())
                .timeout(Duration.ofSeconds(chatModel.getTimeout()))
                .topP(chatModel.getTopP())
                .build();
    }

    protected StreamingChatLanguageModel createLocalAiStreamingChatModel(@NotNull ChatModel chatModel) {
        return LocalAiStreamingChatModel.builder()
                .baseUrl(getModelUrl())
                .modelName(chatModel.getModelName())
                .temperature(chatModel.getTemperature())
                .topP(chatModel.getTopP())
                .timeout(Duration.ofSeconds(chatModel.getTimeout()))
                .build();
    }

    @Override
    public List<LanguageModel> getModels() {
        if (cachedModels != null) {
            return cachedModels;
        }
        List<LanguageModel> modelNames = new ArrayList<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try {
            Object[] models = fetchModels();
            for (Object model : models) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        LanguageModel languageModel = buildLanguageModel(model);
                        synchronized (modelNames) {
                            modelNames.add(languageModel);
                        }
                    } catch (IOException e) {
                        handleModelFetchError(model, e);
                    }
                }, executorService);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            cachedModels = modelNames;
        } catch (IOException e) {
            handleGeneralFetchError(e);
            cachedModels = List.of();
        }
        return cachedModels;
    }

    protected abstract Object[] fetchModels() throws IOException;

    protected abstract LanguageModel buildLanguageModel(Object model) throws IOException;

    protected void handleModelFetchError(Object model, @NotNull IOException e) {
        NotificationUtil.sendNotification(ProjectManager.getInstance().getDefaultProject(), "Error fetching model details: " + e.getMessage());
    }

    protected void handleGeneralFetchError(IOException e) {
        if (!warningShown) {
            NotificationUtil.sendNotification(ProjectManager.getInstance().getDefaultProject(), "Error fetching models: " + e.getMessage());
            warningShown = true;
        }
    }

    @Override
    public void resetModels() {
        cachedModels = null;
    }
}