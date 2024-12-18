package com.axa.jetbrains.ui.panel.chatresponse;

import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.model.request.SemanticFile;
import com.axa.jetbrains.ui.component.ExpandablePanel;

import javax.swing.*;
import java.util.List;

// SemanticSearchReferencesPanel.java
public class SemanticSearchReferencesPanel extends JPanel {
    public SemanticSearchReferencesPanel(ChatMessageContext chatMessageContext, List<SemanticFile> semanticReferences) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        if (semanticReferences != null && !semanticReferences.isEmpty()) {
            ExpandablePanel semanticPanel = new ExpandablePanel(chatMessageContext.getProject(), semanticReferences);
            semanticPanel.setName(chatMessageContext.getId() + "_semantic");
            add(semanticPanel);
        }
    }
}
