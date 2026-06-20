package com.contractai.ai;

import com.contractai.ai.prompts.AiPrompts;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractChatAiService {

    private final ChatModel chatModel;

    public String answer(String question, List<ChunkSearchResult> contextChunks) {
        String context = contextChunks.stream()
                .map(ChunkSearchResult::getChunkText)
                .collect(Collectors.joining("\n---\n"));

        String prompt = AiPrompts.CHAT_SYSTEM +
                "\n\nContext:\n" + context +
                "\n\nQuestion: " + question;

        return chatModel.chat(prompt);
    }
}
