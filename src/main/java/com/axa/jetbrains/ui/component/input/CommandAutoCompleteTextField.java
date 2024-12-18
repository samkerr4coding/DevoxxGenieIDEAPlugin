package com.axa.jetbrains.ui.component.input;

import com.axa.jetbrains.model.CustomPrompt;
import com.axa.jetbrains.service.DevoxxGenieSettingsService;
import com.axa.jetbrains.ui.listener.CustomPromptChangeListener;
import com.axa.jetbrains.ui.settings.DevoxxGenieStateService;
import com.axa.jetbrains.ui.topic.AppTopics;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static com.axa.jetbrains.model.Constant.*;

public class CommandAutoCompleteTextField extends JBTextArea implements CustomPromptChangeListener {

    private static final Logger LOG = Logger.getInstance(CommandAutoCompleteTextField.class);

    private final List<String> commands = new ArrayList<>();
    private final transient Project project;

    private boolean isAutoCompleting = false;
    private String placeholder = "";

    public CommandAutoCompleteTextField(Project project) {
        super();
        this.project = project;
        setDocument(new CommandDocument());
        addKeyListener(new CommandKeyListener());

        ApplicationManager.getApplication()
            .getMessageBus()
            .connect()
            .subscribe(AppTopics.CUSTOM_PROMPT_CHANGED_TOPIC, this);

        initializeCommands();
    }

    private void initializeCommands() {
        commands.clear();
        commands.add("/" + TEST_COMMAND);
        commands.add("/" + EXPLAIN_COMMAND);
        commands.add("/" + REVIEW_COMMAND);
        commands.add("/" + TDG_COMMAND);
        commands.add("/" + HELP_COMMAND);

        DevoxxGenieSettingsService stateService = DevoxxGenieStateService.getInstance();
        for (CustomPrompt customPrompt : stateService.getCustomPrompts()) {
            commands.add("/" + customPrompt.getName());
        }
    }

    private class CommandKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(@NotNull KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isShiftDown()) {
                sendPrompt();
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
                autoComplete();
                e.consume();
            }
        }

        private void sendPrompt() {
            String text = getText().trim();
            if (!text.isEmpty()) {
                ApplicationManager.getApplication().getMessageBus()
                        .syncPublisher(AppTopics.PROMPT_SUBMISSION_TOPIC)
                        .onPromptSubmitted(project, text);
            }
        }

        private void autoComplete() {
            String text = getText();
            String[] lines = text.split("\n");
            if (lines.length > 0 && lines[lines.length - 1].startsWith("/")) {
                String currentLine = lines[lines.length - 1];
                for (String command : commands) {
                    if (command.startsWith(currentLine)) {
                        isAutoCompleting = true;
                        int start = text.lastIndexOf("\n") + 1;
                        try {
                            getDocument().remove(start, currentLine.length());
                            getDocument().insertString(start, command, null);
                        } catch (BadLocationException ex) {
                            LOG.error("Error while auto-completing command", ex);
                        }
                        setCaretPosition(text.length() - currentLine.length() + command.length());
                        isAutoCompleting = false;
                        break;
                    }
                }
            }
        }
    }

    private class CommandDocument extends PlainDocument {
        @Override
        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
            if (isAutoCompleting) {
                super.insertString(offs, str, a);
                return;
            }

            super.insertString(offs, str, a);
            scheduleAutoComplete();
        }

        private void scheduleAutoComplete() {
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    handleAutoComplete();
                } catch (BadLocationException e) {
                    LOG.error("Error while auto-completing command", e);
                }
            });
        }

        private void handleAutoComplete() throws BadLocationException {
            String text = getText(0, getLength());
            String currentLine = getCurrentLine(text);

            if (shouldAttemptAutoComplete(currentLine)) {
                attemptAutoComplete(currentLine);
            }
        }

        private String getCurrentLine(@NotNull String text) {
            String[] lines = text.split("\n");
            return lines.length > 0 ? lines[lines.length - 1] : "";
        }

        private boolean shouldAttemptAutoComplete(@NotNull String currentLine) {
            return currentLine.startsWith("/") && currentLine.length() > 1;
        }

        private void attemptAutoComplete(String currentLine) throws BadLocationException {
            for (String command : commands) {
                if (command.startsWith(currentLine) && !command.equals(currentLine)) {
                    applyAutoComplete(command, currentLine);
                    break;
                }
            }
        }

        private void applyAutoComplete(@NotNull String command,
                                       @NotNull String currentLine) throws BadLocationException {
            String completion = command.substring(currentLine.length());
            isAutoCompleting = true;
            insertString(getLength(), completion, null);
            setCaretPosition(getLength());
            moveCaretPosition(getLength() - completion.length());
            isAutoCompleting = false;
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    public void onCustomPromptsChanged() {
        initializeCommands();
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);
        if (getText().isEmpty() && !placeholder.isEmpty()) {
            g.setColor(JBColor.GRAY);
            g.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
        }
    }
}
