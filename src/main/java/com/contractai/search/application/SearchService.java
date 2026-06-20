package com.contractai.search.application;

import com.contractai.ai.ChunkSearchResult;
import com.contractai.ai.EmbeddingService;
import com.contractai.ai.VectorStoreService;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int TOP_K = 10;

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public SearchResponse search(SearchRequest request) {
        float[] queryEmbedding = embeddingService.embed(request.getQuery());
        List<ChunkSearchResult> results = vectorStoreService.similaritySearch(queryEmbedding, TOP_K, null);

        List<SearchResultItem> items = results.stream()
                .map(r -> SearchResultItem.builder()
                        .contractId(r.getContractId())
                        .contractName(r.getContractName())
                        .matchedChunk(r.getChunkText())
                        .similarity(r.getSimilarity())
                        .build())
                .toList();

        return SearchResponse.builder().results(items).build();
    }

    @Getter
    @Setter
    public static class SearchRequest {
        private String query;
    }

    @Getter
    @Builder
    public static class SearchResponse {
        private List<SearchResultItem> results;
    }

    @Getter
    @Builder
    public static class SearchResultItem {
        private Long contractId;
        private String contractName;
        private String matchedChunk;
        private double similarity;
    }
}
