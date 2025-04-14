package com.devoxx.genie.chatmodel.cloud.azureopenai;

import com.devoxx.genie.chatmodel.ChatModelFactory;
import com.devoxx.genie.model.ChatModel;
import com.devoxx.genie.model.LanguageModel;
import com.devoxx.genie.model.enums.ModelProvider;
import com.devoxx.genie.model.securegpt.ApiModelResponse;
import com.devoxx.genie.service.auth.OAuthService;
import com.devoxx.genie.ui.settings.DevoxxGenieStateService;
import com.devoxx.genie.ui.util.NotificationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.ProjectManager;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AzureOpenAIChatModelFactory implements ChatModelFactory {

    private final ModelProvider MODEL_PROVIDER = ModelProvider.AzureOpenAI;
    private static final String MODELS_ENDPOINT = "https://api-pp.se.axa-go.axa.com/ago-m365-securegpt-bapi-v1-vrs/models";
    protected static boolean warningShown = false;
    private static final Logger LOG = Logger.getInstance(AzureOpenAIChatModelFactory.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ChatLanguageModel createChatModel(@NotNull ChatModel chatModel) {
        boolean isO1 = chatModel.getModelName().startsWith("o1-");

        final var builder = AzureOpenAiChatModel.builder()
                .apiKey(getApiKey(MODEL_PROVIDER))
                .deploymentName(DevoxxGenieStateService.getInstance().getAzureOpenAIDeployment())
                .maxRetries(chatModel.getMaxRetries())
                .timeout(Duration.ofSeconds(chatModel.getTimeout()))
                .topP(isO1 ? 1.0 : chatModel.getTopP())
                .endpoint(DevoxxGenieStateService.getInstance().getAzureOpenAIEndpoint())
                .listeners(getListener());

        return builder.build();
    }

    @Override
    public StreamingChatLanguageModel createStreamingChatModel(@NotNull ChatModel chatModel) {
        boolean isO1 = chatModel.getModelName().startsWith("o1-");

        final var builder = AzureOpenAiStreamingChatModel.builder()
                .apiKey(getApiKey(MODEL_PROVIDER))
                .deploymentName(DevoxxGenieStateService.getInstance().getAzureOpenAIDeployment())
                .timeout(Duration.ofSeconds(chatModel.getTimeout()))
                .topP(isO1 ? 1.0 : chatModel.getTopP())
                .endpoint(DevoxxGenieStateService.getInstance().getAzureOpenAIEndpoint());

        return builder.build();
    }

    public static OAuthService getOAuthService() {
        return ApplicationManager.getApplication().getService(OAuthService.class);
    }

    /**
     * Using the Azure OpenAI provider, models are wrapped in a deployment.
     * There is an API available with which you can list your deployments and info about them,
     * but it's not through the same endpoint and needs a different api key,
     * so we're sadly creating a single mock model for now, which is the name of deployment.
     */
    @Override
    public List<LanguageModel> getModels() {
//        return List.of(LanguageModel.builder()
//                .provider(MODEL_PROVIDER)
//                .modelName(AxaAiStateService.getInstance().getAzureOpenAIDeployment())
//                .displayName(AxaAiStateService.getInstance().getAzureOpenAIDeployment())
//                .inputCost(0.0)
//                .outputCost(0.0)
//                .inputMaxTokens(0)
//                .apiKeyUsed(true)
//                .build());

        return fetchModels();
    }

    private List<LanguageModel> fetchModels() {
        List<LanguageModel> fetchedModels = new ArrayList<LanguageModel>();
        try {
            String accessToken = getOAuthService().getAccessToken();

            if (accessToken == null || accessToken.isEmpty()) {
                LOG.warn("Access token is not available. Cannot refresh models.");

                //TODO TO delete it's only test
                LanguageModel languageModel = new LanguageModel();
                languageModel.setProvider(ModelProvider.AzureOpenAI);
                languageModel.setModelName("bidon");
                languageModel.setDisplayName("bidon");

                fetchedModels.add(languageModel);

                return fetchedModels;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MODELS_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<ApiModelResponse> modelResponses = objectMapper.readValue(response.body(), new TypeReference<>() {});

                for (ApiModelResponse modelResponse : modelResponses) {

                    LanguageModel languageModel = getLanguageModel(modelResponse);

                    fetchedModels.add(languageModel);
                }


            } else {
                LOG.error("Failed to fetch models. Status code: " + response.statusCode() + ", Body: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            handleModelFetchError(e);
        }
        return fetchedModels;
    }

    private static @NotNull LanguageModel getLanguageModel(ApiModelResponse modelResponse) {
        LanguageModel languageModel = new LanguageModel();
        languageModel.setModelName(modelResponse.id());
        languageModel.setDisplayName(modelResponse.id());
//        languageModel.setContextWindow(modelResponse.contextWindow());
//        languageModel.setDeprecationDate(modelResponse.deprecationDate());
//        languageModel.setHasChatCompletions(modelResponse.capabilities().chat_completions());
//        languageModel.setHasChatCompletionsVision(modelResponse.capabilities().chat_completions_vision());
//        languageModel.setHasCompletions(modelResponse.capabilities().completions());
//        languageModel.setHasEmbeddings(modelResponse.capabilities().embeddings());
        return languageModel;
    }

    protected void handleModelFetchError(@NotNull Exception e) {
        if (!warningShown) {
            NotificationUtil.sendNotification(ProjectManager.getInstance().getDefaultProject(), "Error fetching models: " + e.getMessage());
            warningShown = true;
        }
    }
}