// Filename: ModelResponse.java
package com.devoxx.genie.model.securegpt;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiModelResponse(
        String id,
        Capabilities capabilities,
        int contextWindow,
        String deprecationDate
) {
}
