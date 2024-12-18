package com.axa.jetbrains.model.ollama;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OllamaModelDTO {
    private OllamaModelEntryDTO[] models;
}
