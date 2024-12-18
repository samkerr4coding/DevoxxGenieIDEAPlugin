package com.axa.jetbrains.ui.util;

import com.axa.jetbrains.ui.settings.DevoxxGenieStateService;
import com.intellij.ui.scale.JBUIScale;
import org.jetbrains.annotations.NotNull;
import java.util.stream.Collectors;

public class HelpUtil {

    private HelpUtil() {
    }

    public static @NotNull String getHelpMessage() {
        float scaleFactor = JBUIScale.scale(1f);
        return  """
                <html>
                    <head>
                        <style type="text/css">
                            body {
                                font-family: 'Source Code Pro', monospace;
                                zoom: %s;
                            }
                            h2 {
                                margin-bottom: 5px;
                            }
                            p {
                                margin: 0;
                              }
                            ul {
                                margin-bottom: 5px;
                            }
                            li {
                                margin-bottom: 5px;
                            }
                        </style>
                    </head>
                    <body>
                        <h3>Available commands:</h3>
                            <ul>
                                %s
                            </ul>
                    </body>
                </html>
                """.formatted(scaleFactor == 1.0f ? "normal" : scaleFactor * 100 + "%",
                getCustomPromptCommands()
        );
    }

    public static @NotNull String getCustomPromptCommands() {
        return DevoxxGenieStateService.getInstance()
            .getCustomPrompts()
            .stream()
            .map(customPrompt -> "<li>/" + customPrompt.getName() + " : " + customPrompt.getPrompt() + "</li>")
            .collect(Collectors.joining());
    }
}
