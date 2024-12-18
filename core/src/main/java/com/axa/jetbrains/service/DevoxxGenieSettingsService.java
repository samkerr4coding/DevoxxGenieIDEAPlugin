package com.axa.jetbrains.service;

import java.util.List;

import com.axa.jetbrains.model.CustomPrompt;
import com.axa.jetbrains.model.LanguageModel;
import com.axa.jetbrains.model.enumarations.ModelProvider;

public interface DevoxxGenieSettingsService {

    List<CustomPrompt> getCustomPrompts();

    List<LanguageModel> getLanguageModels();

    String getAzureOpenAIEndpoint();

    String getAzureOpenAIDeployment();

    String getAzureOpenAIKey();

    String getSelectedProvider(String projectLocation);

    String getSelectedLanguageModel(String projectLocation);

    Boolean getStreamMode();

    Double getTemperature();

    Double getTopP();

    Integer getTimeout();

    Integer getMaxRetries();

    Integer getChatMemorySize();

    Integer getMaxOutputTokens();

    String getSystemPrompt();

    String getTestPrompt();

    String getReviewPrompt();

    String getExplainPrompt();

    Boolean getExcludeJavaDoc();

    Boolean getUseGitIgnore();

    List<String> getExcludedDirectories();

    List<String> getIncludedFileExtensions();

    Integer getDefaultWindowContext();

    List<String> getExcludedFiles();

    void setCustomPrompts(List<CustomPrompt> customPrompts);

    void setLanguageModels(List<LanguageModel> languageModels);

    void setAzureOpenAIEndpoint(String endpoint);

    void setAzureOpenAIDeployment(String deployment);

    void setAzureOpenAIKey(String key);

    void setSelectedProvider(String projectLocation, String provider);

    void setSelectedLanguageModel(String projectLocation, String model);

    void setStreamMode(Boolean mode);

    void setTemperature(Double temperature);

    void setTopP(Double topP);

    void setTimeout(Integer timeout);

    void setMaxRetries(Integer retries);

    void setChatMemorySize(Integer size);

    void setMaxOutputTokens(Integer tokens);

    void setSystemPrompt(String prompt);

    void setTestPrompt(String prompt);

    void setReviewPrompt(String prompt);

    void setExplainPrompt(String prompt);

    void setExcludeJavaDoc(Boolean exclude);

    void setExcludedDirectories(List<String> directories);

    void setIncludedFileExtensions(List<String> extensions);

    void setDefaultWindowContext(Integer context);

    void setModelWindowContext(ModelProvider provider, String modelName, int windowContext);

    Boolean getShowExecutionTime();

    void setShowExecutionTime(Boolean showExecutionTime);

    void setUseGitIgnore(Boolean useGitIgnore);



}
