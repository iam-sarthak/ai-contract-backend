package com.contractai.chat.infrastructure;

import com.contractai.chat.domain.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    List<ChatHistory> findByContractIdOrderByCreatedAtDesc(Long contractId);
}
