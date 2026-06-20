package com.contractai.risk.application;

import com.contractai.contract.application.ContractService;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.exception.BadRequestException;
import com.contractai.risk.domain.Risk;
import com.contractai.risk.domain.RiskSeverity;
import com.contractai.risk.infrastructure.RiskRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final RiskRepository riskRepository;
    private final ContractService contractService;

    public RiskAnalysisResponse getRisks(Long contractId) {
        var contract = contractService.findContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException("Contract processing is not complete");
        }

        List<RiskItem> risks = riskRepository.findByContractId(contractId).stream()
                .map(this::toItem)
                .toList();

        return RiskAnalysisResponse.builder()
                .riskScore(contract.getRiskScore())
                .risks(risks)
                .build();
    }

    private RiskItem toItem(Risk risk) {
        return RiskItem.builder()
                .id(risk.getId())
                .severity(risk.getSeverity())
                .description(risk.getDescription())
                .recommendation(risk.getRecommendation())
                .build();
    }

    @Getter
    @Builder
    public static class RiskAnalysisResponse {
        private Integer riskScore;
        private List<RiskItem> risks;
    }

    @Getter
    @Builder
    public static class RiskItem {
        private Long id;
        private RiskSeverity severity;
        private String description;
        private String recommendation;
    }
}
