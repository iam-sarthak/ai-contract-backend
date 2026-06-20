package com.contractai.ai;

import com.contractai.ai.prompts.AiPrompts;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContractComparisonAiService {

    private final ChatModel chatModel;

    public String compare(String contractAText, String contractBText) {
        String prompt = AiPrompts.COMPARISON_SYSTEM +
                "\n\nContract A:\n" + truncate(contractAText) +
                "\n\nContract B:\n" + truncate(contractBText);
        return chatModel.chat(prompt);
    }

    private String truncate(String text) {
        if (text.length() <= 15000) {
            return text;
        }
        return text.substring(0, 15000);
    }
}
