package com.contractai.clause.application;

import com.contractai.clause.domain.Clause;
import com.contractai.clause.infrastructure.ClauseRepository;
import com.contractai.contract.application.ContractService;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.exception.BadRequestException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClauseService {

    private final ClauseRepository clauseRepository;
    private final ContractService contractService;

    public List<ClauseResponse> getClauses(Long contractId) {
        var contract = contractService.findContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException("Contract processing is not complete");
        }
        return clauseRepository.findByContractId(contractId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ClauseResponse toResponse(Clause clause) {
        return ClauseResponse.builder()
                .id(clause.getId())
                .type(clause.getType())
                .text(clause.getText())
                .confidence(clause.getConfidence())
                .build();
    }

    @Getter
    @Builder
    public static class ClauseResponse {
        private Long id;
        private com.contractai.clause.domain.ClauseType type;
        private String text;
        private Double confidence;
    }
}
