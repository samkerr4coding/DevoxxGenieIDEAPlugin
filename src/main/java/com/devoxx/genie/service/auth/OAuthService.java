package com.devoxx.genie.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.ClientCredentialsTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public final class OAuthService {

    private static final Logger LOG = Logger.getInstance(OAuthService.class);

    private static final String CLIENT_ID_ENV_VAR = "CLIENT_ID";
    private static final String CLIENT_SECRET_ENV_VAR = "CLIENT_SECRET";
    private static final String SCOPE_ENV_VAR = "SCOPE";
    private static final String ONE_LOGIN_URL_ENV_VAR = "ONE_LOGIN_URL";
    private String accessToken;
    private LocalDateTime tokenCreationTime;
    private int expiresInSeconds;
    private final NetHttpTransport transport = new NetHttpTransport();
    private final GsonFactory jsonFactory = new GsonFactory();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @NotNull
    public static OAuthService getInstance() {
        return com.intellij.openapi.application.ApplicationManager.getApplication().getService(OAuthService.class);
    }

    public OAuthService() {
        // Initialize the access token when the service is created
        try {
            refreshAccessToken();
        } catch (IOException e) {
            LOG.error("Error during initial access token fetch", e);
        }
    }

    public String getAccessToken() {
        // Check if the token is valid, if not refresh it
        try {
            if (accessToken == null || isTokenExpired()) {
                refreshAccessToken();
            }
        } catch (IOException e) {
            LOG.error("Error during access token refresh", e);
            return null;
        }
        return accessToken;
    }

    private boolean isTokenExpired() {
        if (tokenCreationTime == null) {
            return true; // Consider it expired if creation time is not set
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        return now.isAfter(tokenCreationTime.plusSeconds(expiresInSeconds));
    }

    public void refreshAccessToken() throws IOException {
        String clientId = System.getenv(CLIENT_ID_ENV_VAR);
        String clientSecret = System.getenv(CLIENT_SECRET_ENV_VAR);
        String scope = System.getenv(SCOPE_ENV_VAR);
        String oneLoginUrl = System.getenv(ONE_LOGIN_URL_ENV_VAR);

        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty() || scope == null || scope.isEmpty() || oneLoginUrl == null || oneLoginUrl.isEmpty()) {
            LOG.warn("Client ID, Client Secret, Scope or One Login URL environment variables are not set. Cannot refresh access token.");
            return;
        }

        TokenResponse response = fetchAccessToken(oneLoginUrl, clientId, clientSecret, scope);
        this.accessToken = response.getAccessToken();
        this.tokenCreationTime = LocalDateTime.now(ZoneOffset.UTC);
        // Extract expires_in from the response
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            this.expiresInSeconds = jsonNode.get("expires_in").asInt();
        } catch (Exception e) {
            LOG.error("Error during parsing expires_in from token response", e);
            this.expiresInSeconds = 3599; // Default value if parsing fails
        }
    }

    private TokenResponse fetchAccessToken(String authPlatformUrl, String clientId, String clientSecret, String scope) throws IOException {
        ClientCredentialsTokenRequest request = new ClientCredentialsTokenRequest(
                transport,
                jsonFactory,
                new GenericUrl(authPlatformUrl)
        );
        request.setClientAuthentication(new com.google.api.client.auth.oauth2.ClientParametersAuthentication(clientId, clientSecret));
        List<String> scopes = new ArrayList<>();
        scopes.add(scope);
        request.setScopes(scopes);

        return request.execute();
    }
}
