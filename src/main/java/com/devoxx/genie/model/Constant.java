package com.devoxx.genie.model;

public class Constant {
    private Constant() {
    }

    // The fixed command prompts
    public static final String TEST_PROMPT = "Write a unit test for this code using JUnit.";
    public static final String REVIEW_PROMPT = "Review the selected code, can it be improved or are there any bugs?";
    public static final String EXPLAIN_PROMPT = "Break down the code in simple terms to help a junior developer grasp its functionality.";
    public static final String CUSTOM_PROMPT = "Write a custom prompt here.";

    // The Local LLM Model URLs
    public static final String OLLAMA_MODEL_URL = "http://localhost:11434/";
    public static final String LMSTUDIO_MODEL_URL = "http://localhost:1234/v1/";
    public static final String GPT4ALL_MODEL_URL = "http://localhost:4891/v1/";
    public static final String JAN_MODEL_URL = "http://localhost:1337/v1/";

    // The LLM Settings
    public static final Double TEMPERATURE = 0.7d;
    public static final Double TOP_P = 0.9d;
    public static final Integer MAX_OUTPUT_TOKENS = 2500;
    public static final Integer MAX_RETRIES = 3;
    public static final Integer TIMEOUT = 60;
    public static final Integer MAX_MEMORY = 6;
}
