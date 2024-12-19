package com.axa.jetbrains.model;

public class Constant {

    private Constant() {
    }

    // The fixed command prompts
    public static final String SYSTEM_PROMPT = """
        You are a software developer IDEA plugin with expert knowledge in any programming language.
        Do not include any more info which might be incorrect.
        Only provide info that is correct and relevant to the code or plugin.
        """;

    public static final String MARKDOWN = "\nAlways use markdown to format your prompt. For example, use **bold** or *italic* text and ``` code blocks ```.";

    public static final String TEST_PROMPT = "Write a unit test for this code using JUnit.";
    public static final String REVIEW_PROMPT = "Review the selected code, can it be improved or are there any bugs?";
    public static final String EXPLAIN_PROMPT = "Break down the code in simple terms to help a junior developer grasp its functionality.";
    public static final String TDG_PROMPT = "You are a professional Java developer. Give me a SINGLE FILE COMPLETE java implementation that will pass this test. Do not respond with a test. Give me only complete code and no snippets. Include imports and use the right package.";
    public static final String HELP_PROMPT = "Display help and available commands for the Axa AI Plugin";

    public static final String TEST_COMMAND = "test";
    public static final String REVIEW_COMMAND = "review";
    public static final String EXPLAIN_COMMAND = "explain";
    public static final String TDG_COMMAND = "tdg";
    public static final String HELP_COMMAND = "help";


    // ActionCommands
    public static final String SUBMIT_ACTION = "submit";
    public static final String COMBO_BOX_CHANGED = "comboBoxChanged";

    // I18N file name
    public static final String MESSAGES = "messages";

    // The LLM Settings
    public static final Double TEMPERATURE = 0.0d;
    public static final Double TOP_P = 0.9d;
    public static final Integer MAX_OUTPUT_TOKENS = 4000;
    public static final Integer MAX_RETRIES = 1;
    public static final Integer TIMEOUT = 180;
    public static final Integer MAX_MEMORY = 10;


    // Stream mode settings
    public static final Boolean STREAM_MODE = false;

    // Button labels
    public static final String ADD_PROJECT_TO_CONTEXT = "Add project";
    public static final String CALC_TOKENS_COST = "Calc Tokens";
    public static final String REMOVE_CONTEXT = "Remove context";

    // Button tooltip texts
    public static final String SHIFT_ENTER = " (Shift+Enter)";
    public static final String ADD_FILE_S_TO_PROMPT_CONTEXT = "Add file(s) to prompt context";
    public static final String SUBMIT_THE_PROMPT = "Submit the prompt";
    public static final String CALCULATE_TOKENS_COST = "Calculate tokens cost";
    public static final String ADD_ENTIRE_PROJECT_TO_PROMPT_CONTEXT = "Add entire project to prompt context";
    public static final String PROMPT_IS_RUNNING_PLEASE_BE_PATIENT = "Prompt is running, please be patient...";
    public static final String REMOVE_ENTIRE_PROJECT_FROM_PROMPT_CONTEXT = "Remove entire project from prompt context";

    // Titles
    public static final String FILTER_AND_DOUBLE_CLICK_TO_ADD_TO_PROMPT_CONTEXT = "Filter and Double-Click To Add To Prompt Context";
}
