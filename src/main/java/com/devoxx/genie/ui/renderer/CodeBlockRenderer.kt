package com.devoxx.genie.ui.renderer

import com.devoxx.genie.ui.util.LanguageGuesser
import com.intellij.lang.Language
import com.intellij.lang.documentation.DocumentationSettings
import com.intellij.lang.documentation.DocumentationSettings.InlineCodeHighlightingMode.NO_HIGHLIGHTING
import com.intellij.lang.documentation.DocumentationSettings.InlineCodeHighlightingMode.SEMANTIC_HIGHLIGHTING
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import kotlinx.html.ButtonType
import org.commonmark.node.Code
import org.commonmark.node.FencedCodeBlock
import org.commonmark.node.IndentedCodeBlock
import org.commonmark.node.Node
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererContext

/**
 * Special commonmark renderer for code blocks.
 *
 * IJ has a nifty utility [HtmlSyntaxInfoUtil] that is used to copy
 * selected code as HTML, it also happens this utility is used to generate
 * the HTML for the code blocks in the documentation.
 *
 * Use this way
 *
 * ```kotlin
 * val node = Parser.builder().build().parse(markdownContent)
 * val renderer = HtmlRenderer.builder()
 *    .nodeRendererFactory { context -> CodeBlockNodeRenderer(project, context) }
 *    .build()
 *
 * val html = renderer.render(node)
 * ```
 *
 * @see com.intellij.codeInsight.javadoc.JavaDocInfoGenerator.generateCodeValue
 * @see com.intellij.codeInsight.javadoc.JavaDocInfoGenerator.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet
 * @see KDocRenderer StringBuilder.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet
 */
@Suppress("UnstableApiUsage")
class CodeBlockNodeRenderer(
    private val project: Project,
    context: HtmlNodeRendererContext
) : NodeRenderer {
    private val htmlOutputWriter = context.writer

    override fun render(node: Node) {
        when (node) {
            is IndentedCodeBlock -> {
                renderCode(node.literal, block = true)
            }
            is FencedCodeBlock -> {
                renderCode(node.literal, info = node.info, block = true)
            }
            is Code -> {
                renderCode(node.literal)
            }
            else -> {
                System.err.println("Unknown node type: $node")
            }
        }
    }

    override fun getNodeTypes() = setOf(
        IndentedCodeBlock::class.java,
        FencedCodeBlock::class.java,
        Code::class.java
    )

    private enum class HighlightingMode {
        SEMANTIC_HIGHLIGHTING,
        NO_HIGHLIGHTING,
        INLINE_HIGHLIGHTING  // Assuming this mode exists
    }

    private fun determineHighlightingMode(block: Boolean): HighlightingMode {
        return when {
            block && DocumentationSettings.isHighlightingOfCodeBlocksEnabled() -> HighlightingMode.SEMANTIC_HIGHLIGHTING
            block -> HighlightingMode.NO_HIGHLIGHTING
            else -> HighlightingMode.INLINE_HIGHLIGHTING
        }
    }

    // FIX_WHEN_MIN_IS_241 Note after 241, we may consider `com.intellij.lang.documentation.QuickDocHighlightingHelper`
    // `DocumentationSettings.getMonospaceFontSizeCorrection` is going away possibly 242.
    // Note that styled HTML code would then be `div.styled-code > pre`
    private fun renderCode(codeSnippet: String, info: String = "", block: Boolean = false) {
        htmlOutputWriter.line()

        if (block) {
            htmlOutputWriter.tag("pre")
        }
        htmlOutputWriter.tag("code style='font-size:14pt'")

        val highlightingMode = determineHighlightingMode(block)

        htmlOutputWriter.raw(
            appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                highlightingMode,
                project,
                LanguageGuesser.guessLanguage(info) ?: PlainTextLanguage.INSTANCE,
                codeSnippet
            )
        )
        htmlOutputWriter.tag("/code")
        if (block) {
            htmlOutputWriter.tag("/pre")
        }

        htmlOutputWriter.line()
    }

    /**
     * Inspired by KDocRenderer.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet
     */
    private fun appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
        highlightingMode: HighlightingMode,
        project: Project,
        language: Language,
        codeSnippet: String
    ): String {
        var highlightedAndEncodedAsHtmlCodeSnippet = buildString {
            when (highlightingMode) {
                HighlightingMode.SEMANTIC_HIGHLIGHTING -> {
                    ApplicationManager.getApplication().runReadAction {
                        HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet(
                            this,
                            project,
                            language,
                            codeSnippet,
                            false,
                            DocumentationSettings.getHighlightingSaturation(true)
                        )
                    }
                }
                else -> {
                    // raw code snippet, but escaped
                    append(StringUtil.escapeXmlEntities(codeSnippet))
                }
            }
        }

        if (highlightingMode != HighlightingMode.NO_HIGHLIGHTING) {
            // set code text color as editor default code color instead of doc component text color
            // surround by a span using the same editor colors
            val codeAttributes = EditorColorsManager.getInstance()
                .globalScheme
                .getAttributes(HighlighterColors.TEXT)
                .clone()
                .apply { backgroundColor = null }

            highlightedAndEncodedAsHtmlCodeSnippet = buildString {
                HtmlSyntaxInfoUtil.appendStyledSpan(
                    this,
                    codeAttributes,
                    highlightedAndEncodedAsHtmlCodeSnippet,
                    DocumentationSettings.getHighlightingSaturation(true)
                )
            }
        }

        return highlightedAndEncodedAsHtmlCodeSnippet
    }
}
