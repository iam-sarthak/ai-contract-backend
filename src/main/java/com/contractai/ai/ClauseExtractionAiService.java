package com.contractai.ai;

import com.contractai.ai.prompts.AiPrompts;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class ClauseExtractionAiService {

    private final ChatModel chatModel;

    public ClauseExtractionAiService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String extractClauses(String contractText) {
        String prompt = AiPrompts.CLAUSE_EXTRACTION_SYSTEM + "\n\nContract text:\n" + truncate(contractText);
        return chatModel.chat(prompt);
    }

    private String truncate(String text) {
        if (text.length() <= 30000) {
            return text;
        }
        return text.substring(0, 30000);
    }
}
