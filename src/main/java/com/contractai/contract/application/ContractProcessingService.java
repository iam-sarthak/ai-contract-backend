package com.contractai.contract.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractProcessingService {

    private final ContractProcessor contractProcessor;

    @Async("taskExecutor")
    public void processAsync(Long contractId) {
        try {
            contractProcessor.process(contractId);
        } catch (Exception e) {
            log.error("Contract processing failed for contractId={}", contractId, e);
            contractProcessor.markFailed(contractId);
        }
    }
}
