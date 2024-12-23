package com.devoxx.genie.action;

import com.devoxx.genie.ui.util.DevoxxGenieIconsUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class InlineChatAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            showInlineInput(editor);
        }
    }

    private void showInlineInput(Editor editor) {
        // Create a text field for input
        JBTextField inputField = new JBTextField();
        inputField.setColumns(20);

        // Create a title panel with icon and title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(UIManager.getColor("Popup.background")); // Match the popup background
        JLabel iconLabel = new JLabel(DevoxxGenieIconsUtil.DevoxxIcon);
        JLabel titleLabel = new JLabel(" Devoxx Chat"); // Adding space for separation

        titlePanel.add(iconLabel);
        titlePanel.add(titleLabel);

        // Create a popup to show the input field
        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(inputField, inputField)
                .setTitle("Devoxx Chat") // You still set a title for accessibility
                .setRequestFocus(true)
                .setResizable(false)
                .setCancelOnClickOutside(true)
                .setMovable(true)
                .createPopup();

        // Show the title panel by adding it to the popup component
        popup.getContent().add(titlePanel, BorderLayout.NORTH);

        // Calculate popup position
        Point popupPosition = calculatePopupPosition(editor, popup);

        // Show the popup at the calculated position
        popup.show(new RelativePoint(editor.getComponent(), popupPosition));

        // Add action listener to handle input submission
        inputField.addActionListener((ActionEvent e) -> {
            String prompt = inputField.getText();
            // Call your AI model with the prompt
            String aiResponse = callAIModel(prompt);
            insertResponseIntoEditor(editor, aiResponse);
            popup.closeOk(null); // Close the popup after submission
        });
    }

    private Point calculatePopupPosition(Editor editor, JBPopup popup) {
        SelectionModel selectionModel = editor.getSelectionModel();
        if (selectionModel.hasSelection()) {
            // Get the logical position at the end of the selection
            LogicalPosition endLogicalPosition = editor.offsetToLogicalPosition(selectionModel.getSelectionEnd());
            // Convert the logical position to a visual position
            VisualPosition endVisualPosition = editor.logicalToVisualPosition(endLogicalPosition);
            // Convert the visual position to a Point
            Point endPoint = editor.visualPositionToXY(endVisualPosition);

            // Calculate the height of a line in the editor
            int lineHeight = editor.getLineHeight();

            // Position the popup just below the end of the selection
            return new Point(endPoint.x, endPoint.y + lineHeight); // Move down by one line height
        } else {
            // If no selection, position at the top-right corner of the editor
            Dimension editorSize = editor.getComponent().getSize();
            return new Point(editorSize.width - 200, 5); // Example: 200 pixels from the right, 5 pixels down from the top
        }
    }


    private String callAIModel(String prompt) {
        // Implement API call logic here
        return "AI generated response"; // Replace with actual response
    }

    private void insertResponseIntoEditor(Editor editor, String response) {
        Document document = editor.getDocument();
        int caretOffset = editor.getCaretModel().getOffset();
        // Insert the response at the caret position
        SwingUtilities.invokeLater(() -> {
            document.insertString(caretOffset, response + "\n");
        });
    }
}
