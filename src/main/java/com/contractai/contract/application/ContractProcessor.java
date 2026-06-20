package com.contractai.contract.application;

import com.contractai.ai.AiResponseParser;
import com.contractai.ai.ClauseExtractionAiService;
import com.contractai.ai.ContractSummaryAiService;
import com.contractai.ai.EmbeddingService;
import com.contractai.ai.RiskAnalysisAiService;
import com.contractai.ai.VectorStoreService;
import com.contractai.clause.domain.Clause;
import com.contractai.clause.domain.ClauseType;
import com.contractai.clause.infrastructure.ClauseRepository;
import com.contractai.contract.domain.Contract;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.contract.infrastructure.ContractRepository;
import com.contractai.document.DocumentTextExtractor;
import com.contractai.document.TextChunker;
import com.contractai.document.TextCleaner;
import com.contractai.risk.domain.Risk;
import com.contractai.risk.domain.RiskSeverity;
import com.contractai.risk.infrastructure.RiskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractProcessor {

    private final ContractRepository contractRepository;
    private final DocumentTextExtractor documentTextExtractor;
    private final TextCleaner textCleaner;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final ContractSummaryAiService summaryAiService;
    private final ClauseExtractionAiService clauseExtractionAiService;
    private final RiskAnalysisAiService riskAnalysisAiService;
    private final ClauseRepository clauseRepository;
    private final RiskRepository riskRepository;
    private final AiResponseParser aiResponseParser;

    @Transactional
    public void process(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalStateException("Contract not found: " + contractId));

        contract.setStatus(ContractStatus.PROCESSING);
        contractRepository.save(contract);

        log.info("Processing contractId={}", contractId);

        String rawText = documentTextExtractor.extract(Paths.get(contract.getFilePath()));
        String cleanedText = textCleaner.clean(rawText);

        if (cleanedText.isBlank()) {
            throw new IllegalStateException("No text extracted from document");
        }

        List<String> chunks = textChunker.chunk(cleanedText);
        int chunkIndex = 0;
        for (String chunk : chunks) {
            float[] embedding = embeddingService.embed(chunk);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("chunkIndex", chunkIndex++);
            vectorStoreService.storeEmbedding(contractId, chunk, embedding, metadata);
        }

        String summaryJson = summaryAiService.generateSummary(cleanedText);
        contract.setSummary(summaryJson);

        extractAndSaveClauses(contract, cleanedText);
        analyzeAndSaveRisks(contract, cleanedText);

        contract.setStatus(ContractStatus.COMPLETED);
        contractRepository.save(contract);

        log.info("Contract processing completed for contractId={}", contractId);
    }

    @Transactional
    public void markFailed(Long contractId) {
        contractRepository.findById(contractId).ifPresent(contract -> {
            contract.setStatus(ContractStatus.FAILED);
            contractRepository.save(contract);
        });
    }

    private void extractAndSaveClauses(Contract contract, String text) {
        clauseRepository.deleteByContractId(contract.getId());
        String response = clauseExtractionAiService.extractClauses(text);
        JsonNode clauses = aiResponseParser.parseJson(response);

        if (!clauses.isArray()) {
            return;
        }

        for (JsonNode node : clauses) {
            String typeStr = node.path("type").asText();
            String clauseText = node.path("text").asText();
            double confidence = node.path("confidence").asDouble(0.5);

            if (clauseText.isBlank()) {
                continue;
            }

            try {
                ClauseType type = ClauseType.valueOf(typeStr);
                Clause clause = Clause.builder()
                        .contract(contract)
                        .type(type)
                        .text(clauseText)
                        .confidence(confidence)
                        .build();
                clauseRepository.save(clause);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown clause type: {}", typeStr);
            }
        }
    }

    private void analyzeAndSaveRisks(Contract contract, String text) {
        riskRepository.deleteByContractId(contract.getId());
        String response = riskAnalysisAiService.analyzeRisks(text);
        JsonNode root = aiResponseParser.parseJson(response);

        contract.setRiskScore(root.path("riskScore").asInt(0));

        JsonNode risks = root.path("risks");
        if (!risks.isArray()) {
            return;
        }

        for (JsonNode node : risks) {
            String severityStr = node.path("severity").asText("MEDIUM");
            String description = node.path("description").asText();
            String recommendation = node.path("recommendation").asText();

            if (description.isBlank()) {
                continue;
            }

            try {
                RiskSeverity severity = RiskSeverity.valueOf(severityStr);
                Risk risk = Risk.builder()
                        .contract(contract)
                        .severity(severity)
                        .description(description)
                        .recommendation(recommendation.isBlank() ? "Review with legal counsel" : recommendation)
                        .build();
                riskRepository.save(risk);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown risk severity: {}", severityStr);
            }
        }
    }
}
