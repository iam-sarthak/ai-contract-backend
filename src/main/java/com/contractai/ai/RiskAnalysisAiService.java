package com.contractai.ai;

import com.contractai.ai.prompts.AiPrompts;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskAnalysisAiService {

    private final ChatModel chatModel;

    public String analyzeRisks(String contractText) {
        String prompt = AiPrompts.RISK_ANALYSIS_SYSTEM + "\n\nContract text:\n" + truncate(contractText);
        return chatModel.chat(prompt);
    }

    private String truncate(String text) {
        if (text.length() <= 30000) {
            return text;
        }
        return text.substring(0, 30000);
    }
}
