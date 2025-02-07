package com.devoxx.genie.service;

import com.devoxx.genie.model.LanguageModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public final class LLMModelRegistryService {

    private Map<String, LanguageModel> models = new HashMap<>();

    @NotNull
    public static LLMModelRegistryService getInstance() {
        return ApplicationManager.getApplication().getService(LLMModelRegistryService.class);
    }

    public LLMModelRegistryService() {
    }

    @NotNull
    public List<LanguageModel> getModels() {
        Map<String, LanguageModel> modelsCopy = new HashMap<>(models);
        return new ArrayList<>(modelsCopy.values());
    }

    public void setModels(Map<String, LanguageModel> models) {
        this.models = new HashMap<>(models);
    }
}
