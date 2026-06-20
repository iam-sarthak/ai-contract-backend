package com.contractai.contract.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompareContractsResponse {

    private Long comparisonId;
    private String report;
}
