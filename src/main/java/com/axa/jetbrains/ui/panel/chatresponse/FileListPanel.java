package com.axa.jetbrains.ui.panel.chatresponse;

import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.ui.component.ExpandablePanel;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.List;

// FileListPanel.java
public class FileListPanel extends JPanel {
    public FileListPanel(ChatMessageContext chatMessageContext, List<VirtualFile> files) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        if (chatMessageContext.hasFiles()) {
            add(new ExpandablePanel(chatMessageContext, files));
        }
    }
}
