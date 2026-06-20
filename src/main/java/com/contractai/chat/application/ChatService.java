package com.contractai.chat.application;

import com.contractai.ai.ChunkSearchResult;
import com.contractai.ai.ContractChatAiService;
import com.contractai.ai.EmbeddingService;
import com.contractai.ai.VectorStoreService;
import com.contractai.auth.domain.User;
import com.contractai.chat.domain.ChatHistory;
import com.contractai.chat.infrastructure.ChatHistoryRepository;
import com.contractai.contract.application.ContractService;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.exception.BadRequestException;
import com.contractai.security.SecurityUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final int TOP_K = 5;

    private final ContractService contractService;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ContractChatAiService chatAiService;
    private final ChatHistoryRepository chatHistoryRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ChatResponse chat(Long contractId, ChatRequest request) {
        var contract = contractService.findContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException("Contract processing is not complete");
        }

        float[] queryEmbedding = embeddingService.embed(request.getQuestion());
        List<ChunkSearchResult> chunks = vectorStoreService.similaritySearch(queryEmbedding, TOP_K, contractId);

        String answer = chatAiService.answer(request.getQuestion(), chunks);

        User user = securityUtils.getCurrentUser();
        ChatHistory history = ChatHistory.builder()
                .contract(contract)
                .user(user)
                .question(request.getQuestion())
                .answer(answer)
                .build();
        chatHistoryRepository.save(history);

        return ChatResponse.builder().answer(answer).build();
    }

    @Getter
    @Setter
    public static class ChatRequest {
        private String question;
    }

    @Getter
    @Builder
    public static class ChatResponse {
        private String answer;
    }
}
