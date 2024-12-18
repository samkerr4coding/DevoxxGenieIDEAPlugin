package com.axa.jetbrains.ui.topic;

import com.axa.jetbrains.ui.listener.*;
import com.axa.jetbrains.ui.listener.*;
import com.intellij.util.messages.Topic;

public class AppTopics {

    public static final Topic<SettingsChangeListener> SETTINGS_CHANGED_TOPIC =
        Topic.create("SettingsChanged", SettingsChangeListener.class);

    public static final Topic<ChatMemorySizeListener> CHAT_MEMORY_SIZE_TOPIC =
        new Topic<>("chatMemorySizeChanged", ChatMemorySizeListener.class);

    public static final Topic<LLMSettingsChangeListener> LLM_SETTINGS_CHANGED_TOPIC =
        Topic.create("LLMSettingsChanged", LLMSettingsChangeListener.class);

    public static final Topic<CustomPromptChangeListener> CUSTOM_PROMPT_CHANGED_TOPIC =
        Topic.create("CustomPromptChanged", CustomPromptChangeListener.class);

    public static final Topic<PromptSubmissionListener> PROMPT_SUBMISSION_TOPIC =
        Topic.create("PromptSubmission", PromptSubmissionListener.class);

    public static final Topic<ConversationEventListener> CONVERSATION_TOPIC =
        Topic.create("NewConversation", ConversationEventListener.class);

    public static final Topic<GitDiffStateListener> GITDIFF_STATE_TOPIC =
            Topic.create("GitDiffState", GitDiffStateListener.class);

}
