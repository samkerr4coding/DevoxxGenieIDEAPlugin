package com.devoxx.genie.util;

import com.devoxx.genie.model.enums.ModelProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultLLMSettingsUtil {

    public static final Map<CostKey, Double> DEFAULT_INPUT_COSTS = new HashMap<>();
    public static final Map<CostKey, Double> DEFAULT_OUTPUT_COSTS = new HashMap<>();

    /**
     * Does the ModelProvider use an API KEY?
     *
     * @param provider the LLM provider
     * @return true when API Key is required, meaning a cost is involved
     */
    public static boolean isApiKeyBasedProvider(ModelProvider provider) {
        return provider == ModelProvider.AzureOpenAI;
    }

    public record CostKey(ModelProvider provider, String modelName) {

        @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                CostKey costKey = (CostKey) o;
                return provider == costKey.provider &&
                    Objects.equals(modelName, costKey.modelName);
            }

        @Override
            public String toString() {
                return "CostKey{provider=" + provider + ", modelName='" + modelName + "'}";
            }
        }
}
