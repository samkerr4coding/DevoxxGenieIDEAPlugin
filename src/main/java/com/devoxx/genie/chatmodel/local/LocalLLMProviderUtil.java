package com.devoxx.genie.chatmodel.local;

import com.devoxx.genie.service.exception.UnsuccessfulRequestException;
import com.devoxx.genie.ui.settings.DevoxxGenieStateService;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

import static com.devoxx.genie.util.HttpUtil.ensureEndsWithSlash;

public class LocalLLMProviderUtil {

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static <T> T getModels(String baseUrlConfigKey, String endpoint, Class<T> responseType) throws IOException {
        String configValue = DevoxxGenieStateService.getInstance().getConfigValue(baseUrlConfigKey);
        String baseUrl = ensureEndsWithSlash(Objects.requireNonNull(configValue));

        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new UnsuccessfulRequestException("Unexpected code " + response);
            }

            if (response.body() == null) {
                throw new UnsuccessfulRequestException("Response body is null");
            }

            String json = response.body().string();

            return gson.fromJson(json, responseType);
        }
    }
}
