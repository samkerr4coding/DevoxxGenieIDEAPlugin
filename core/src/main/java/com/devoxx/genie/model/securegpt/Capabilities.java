// Filename: Capabilities.java
package com.devoxx.genie.model.securegpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Capabilities(
        boolean chat_completions,
        boolean chat_completions_vision,
        boolean completions,
        boolean embeddings
) {
}
