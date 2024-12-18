package com.axa.jetbrains.ui.processor;

import com.axa.jetbrains.model.request.ChatMessageContext;
import com.axa.jetbrains.ui.component.StyleSheetsFactory;
import com.axa.jetbrains.ui.util.CodeSnippetAction;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.renderer.html.HtmlRenderer;

import javax.swing.*;
import java.awt.*;

import static com.axa.jetbrains.ui.util.DevoxxGenieColorsUtil.CODE_BG_COLOR;
import static com.axa.jetbrains.ui.util.DevoxxGenieColorsUtil.CODE_BORDER_BG_COLOR;

public class FencedCodeBlockProcessor implements NodeProcessor {

    private final ChatMessageContext chatMessageContext;
    private final FencedCodeBlock fencedCodeBlock;
    private final CodeSnippetAction codeSnippetAction;

    public FencedCodeBlockProcessor(ChatMessageContext chatMessageContext,
                                    FencedCodeBlock fencedCodeBlock) {
        this.chatMessageContext = chatMessageContext;
        this.fencedCodeBlock = fencedCodeBlock;
        this.codeSnippetAction = new CodeSnippetAction(chatMessageContext);
    }

    /**
     * Process the fenced code block.
     * @return the panel
     */
    @Override
    public JPanel processNode() {
        HtmlRenderer htmlRenderer = createHtmlRenderer(chatMessageContext.getProject());
        String htmlOutput = htmlRenderer.render(fencedCodeBlock);
        JEditorPane editorPane = createEditorPane(htmlOutput, StyleSheetsFactory.createCodeStyleSheet());
        editorPane.setBorder(BorderFactory.createLineBorder(CODE_BORDER_BG_COLOR, 1));

        // Initialize the overlay panel and set the OverlayLayout correctly
        JPanel overlayPanel = new JPanel(new BorderLayout());
        overlayPanel.setBackground(CODE_BG_COLOR);
        overlayPanel.setOpaque(true);

        // Add components to the overlay panel in the correct order
        overlayPanel.add(editorPane, BorderLayout.CENTER);
        overlayPanel.add(codeSnippetAction.createClipBoardButtonPanel(chatMessageContext, fencedCodeBlock), BorderLayout.NORTH);

        return overlayPanel;
    }
}
