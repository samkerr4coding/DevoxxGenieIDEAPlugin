package com.axa.jetbrains.ui.panel;

import com.axa.jetbrains.service.FileListManager;
import com.axa.jetbrains.service.FileListObserver;
import com.axa.jetbrains.ui.component.FileEntryComponent;
import com.axa.jetbrains.ui.listener.FileRemoveListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

/**
 * Here we have a panel that displays a list of files that are selected by the user.
 * These files are used as context for the prompt input.
 */
public class PromptContextFileListPanel extends JPanel
    implements FileRemoveListener, FileListObserver {

    private final FileListManager fileListManager;
    private final JBScrollPane filesScrollPane;
    private final JPanel filesPanel; // new panel for files
    private final Project project;

    public PromptContextFileListPanel(Project project) {
        this.project = project;
        fileListManager = FileListManager.getInstance();
        fileListManager.addObserver(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        filesPanel = new JPanel();
        filesPanel.setLayout(new BoxLayout(filesPanel, BoxLayout.Y_AXIS));

        // Wrap the filesPanel in a JBScrollPane
        filesScrollPane = new JBScrollPane(filesPanel);
        filesScrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
        filesScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        filesScrollPane.setMinimumSize(new Dimension(0, 60));
        filesScrollPane.setPreferredSize(new Dimension(0, 60));
        filesScrollPane.setBorder(null);
        filesScrollPane.setVisible(false);

        add(filesScrollPane);
    }

    @Override
    public void fileAdded(VirtualFile file) {
        updateFilesPanelVisibility();
        FileEntryComponent fileLabel = new FileEntryComponent(project, file, this);
        filesPanel.add(fileLabel);
        updateUIState();
    }

    @Override
    public void filesAdded(java.util.@NotNull List<VirtualFile> files) {
        for (VirtualFile file : files) {
            FileEntryComponent fileLabel = new FileEntryComponent(project, file, this);
            filesPanel.add(fileLabel);
        }
        updateFilesPanelVisibility();
        updateUIState();
    }

    @Override
    public void allFilesRemoved() {
        filesPanel.removeAll();
        updateFilesPanelVisibility();
        updateUIState();
    }

    private void updateFilesPanelVisibility() {
        if (fileListManager.isEmpty()) {
            filesScrollPane.setVisible(false);
            filesScrollPane.setPreferredSize(new Dimension(0, 0));
        } else {
            filesScrollPane.setVisible(true);
            int MAX_VISIBLE_FILES = 3;
            int fileCount = Math.min(fileListManager.size(), MAX_VISIBLE_FILES);
            int heightPerFile = 30;
            int prefHeight = fileCount * heightPerFile;
            filesScrollPane.setPreferredSize(new Dimension(getPreferredSize().width, prefHeight));
        }
        filesScrollPane.revalidate();
        filesScrollPane.repaint();
    }

    @Override
    public void onFileRemoved(VirtualFile file) {
        fileListManager.removeFile(file);
        removeFromFilesPanel(file);
        updateFilesPanelVisibility();
        updateUIState();
    }

    private void removeFromFilesPanel(VirtualFile file) {
        for (Component component : filesPanel.getComponents()) {
            if (component instanceof FileEntryComponent fileEntryComponent &&
                fileEntryComponent.getVirtualFile().equals(file)) {
                filesPanel.remove(fileEntryComponent);
                break;
            }
        }
    }

    private void updateUIState() {
        revalidate();
        repaint();
    }
}
