package com.contractai.contract.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareContractsRequest {

    @NotNull
    private Long contractAId;

    @NotNull
    private Long contractBId;
}
