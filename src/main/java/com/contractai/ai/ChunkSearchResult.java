package com.contractai.ai;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ChunkSearchResult {

    private Long id;
    private Long contractId;
    private String contractName;
    private String chunkText;
    private Map<String, Object> metadata;
    private double similarity;
}
