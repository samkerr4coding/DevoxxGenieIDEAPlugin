package com.axa.jetbrains.ui.listener;

import com.axa.jetbrains.model.request.ChatMessageContext;

public interface ConversationEventListener {
    void onNewConversation(ChatMessageContext chatMessageContext);
}
