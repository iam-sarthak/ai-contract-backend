package com.contractai.contract.web.dto;

import com.contractai.contract.domain.ContractStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContractResponse {

    private Long id;
    private String name;
    private String fileName;
    private ContractStatus status;
    private String summary;
    private Integer riskScore;
    private String uploadedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
