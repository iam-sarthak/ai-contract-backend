package com.contractai.chat.web;

import com.contractai.chat.application.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{contractId}")
    public ChatService.ChatResponse chat(
            @PathVariable Long contractId,
            @Valid @RequestBody ChatRequestDto request) {
        ChatService.ChatRequest chatRequest = new ChatService.ChatRequest();
        chatRequest.setQuestion(request.getQuestion());
        return chatService.chat(contractId, chatRequest);
    }

    @Getter
    @Setter
    public static class ChatRequestDto {
        @NotBlank
        private String question;
    }
}
