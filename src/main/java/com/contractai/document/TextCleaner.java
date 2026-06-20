package com.contractai.document;

import org.springframework.stereotype.Component;

@Component
public class TextCleaner {

    public String clean(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("(?m)^\\s*\\d+\\s*$", "")
                .trim();
    }
}
