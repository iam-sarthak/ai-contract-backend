package com.contractai.contract.web.dto;

import com.contractai.contract.domain.Contract;
import org.springframework.stereotype.Component;

@Component
public class ContractMapper {

    public ContractResponse toResponse(Contract contract) {
        return ContractResponse.builder()
                .id(contract.getId())
                .name(contract.getName())
                .fileName(contract.getFileName())
                .status(contract.getStatus())
                .summary(contract.getSummary())
                .riskScore(contract.getRiskScore())
                .uploadedBy(contract.getUploadedBy().getEmail())
                .createdAt(contract.getCreatedAt())
                .updatedAt(contract.getUpdatedAt())
                .build();
    }
}
