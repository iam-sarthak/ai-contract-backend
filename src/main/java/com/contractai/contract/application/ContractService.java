package com.contractai.contract.application;

import com.contractai.ai.ContractComparisonAiService;
import com.contractai.auth.domain.User;
import com.contractai.clause.infrastructure.ClauseRepository;
import com.contractai.common.PageMapper;
import com.contractai.common.PageResponse;
import com.contractai.contract.domain.Contract;
import com.contractai.contract.domain.ContractComparison;
import com.contractai.contract.domain.ContractStatus;
import com.contractai.contract.infrastructure.ContractComparisonRepository;
import com.contractai.contract.infrastructure.ContractRepository;
import com.contractai.contract.web.dto.CompareContractsRequest;
import com.contractai.contract.web.dto.CompareContractsResponse;
import com.contractai.contract.web.dto.ContractMapper;
import com.contractai.contract.web.dto.ContractResponse;
import com.contractai.document.DocumentTextExtractor;
import com.contractai.document.LocalFileStorageService;
import com.contractai.document.TextCleaner;
import com.contractai.exception.BadRequestException;
import com.contractai.exception.ResourceNotFoundException;
import com.contractai.risk.infrastructure.RiskRepository;
import com.contractai.security.SecurityUtils;
import com.contractai.ai.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractComparisonRepository comparisonRepository;
    private final ClauseRepository clauseRepository;
    private final RiskRepository riskRepository;
    private final LocalFileStorageService fileStorageService;
    private final VectorStoreService vectorStoreService;
    private final ContractProcessingService processingService;
    private final ContractComparisonAiService comparisonAiService;
    private final DocumentTextExtractor documentTextExtractor;
    private final TextCleaner textCleaner;
    private final ContractMapper contractMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    public ContractResponse upload(MultipartFile file) {
        User user = securityUtils.getCurrentUser();

        Contract contract = Contract.builder()
                .name(file.getOriginalFilename())
                .fileName(file.getOriginalFilename())
                .filePath("pending")
                .status(ContractStatus.UPLOADING)
                .uploadedBy(user)
                .build();

        contract = contractRepository.save(contract);

        String filePath = fileStorageService.store(file, contract.getId());
        contract.setFilePath(filePath);
        contract = contractRepository.save(contract);

        Long contractId = contract.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                processingService.processAsync(contractId);
            }
        });

        log.info("Contract uploaded contractId={} by user={}", contract.getId(), user.getEmail());
        return contractMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public PageResponse<ContractResponse> list(ContractStatus status, Pageable pageable) {
        Page<Contract> page = status != null
                ? contractRepository.findByStatus(status, pageable)
                : contractRepository.findAll(pageable);
        return PageMapper.toPageResponse(page, contractMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ContractResponse getById(Long id) {
        Contract contract = findContractOrThrow(id);
        return contractMapper.toResponse(contract);
    }

    @Transactional
    public void delete(Long id) {
        Contract contract = findContractOrThrow(id);
        User user = securityUtils.getCurrentUser();

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));
        boolean isOwner = contract.getUploadedBy().getId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Not authorized to delete this contract");
        }

        vectorStoreService.deleteByContractId(id);
        clauseRepository.deleteByContractId(id);
        riskRepository.deleteByContractId(id);
        fileStorageService.delete(contract.getFilePath());
        contractRepository.delete(contract);

        log.info("Contract deleted contractId={}", id);
    }

    @Transactional
    public CompareContractsResponse compare(CompareContractsRequest request) {
        if (request.getContractAId().equals(request.getContractBId())) {
            throw new BadRequestException("Cannot compare a contract with itself");
        }

        Contract contractA = findContractOrThrow(request.getContractAId());
        Contract contractB = findContractOrThrow(request.getContractBId());

        if (contractA.getStatus() != ContractStatus.COMPLETED || contractB.getStatus() != ContractStatus.COMPLETED) {
            throw new BadRequestException("Both contracts must be in COMPLETED status");
        }

        String textA = textCleaner.clean(documentTextExtractor.extract(Paths.get(contractA.getFilePath())));
        String textB = textCleaner.clean(documentTextExtractor.extract(Paths.get(contractB.getFilePath())));

        String report = comparisonAiService.compare(textA, textB);

        ContractComparison comparison = ContractComparison.builder()
                .contractA(contractA)
                .contractB(contractB)
                .report(report)
                .createdBy(securityUtils.getCurrentUser())
                .build();

        comparison = comparisonRepository.save(comparison);

        return CompareContractsResponse.builder()
                .comparisonId(comparison.getId())
                .report(report)
                .build();
    }

    public Contract findContractOrThrow(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
    }
}
