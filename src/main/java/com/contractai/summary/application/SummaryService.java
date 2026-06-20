package com.contractai.summary.application;

import com.contractai.ai.AiResponseParser;
import com.contractai.contract.application.ContractService;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.exception.BadRequestException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final ContractService contractService;
    private final AiResponseParser aiResponseParser;

    public JsonNode getSummary(Long contractId) {
        var contract = contractService.findContractOrThrow(contractId);
        if (contract.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException("Contract processing is not complete");
        }
        if (contract.getSummary() == null || contract.getSummary().isBlank()) {
            throw new BadRequestException("Summary not available for this contract");
        }
        return aiResponseParser.parseJson(contract.getSummary());
    }
}
