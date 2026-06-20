package com.contractai.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public void storeEmbedding(Long contractId, String chunkText, float[] embedding, Map<String, Object> metadata) {
        String vectorLiteral = toVectorLiteral(embedding);
        String metadataJson = toJson(metadata);

        jdbcTemplate.update(
                "INSERT INTO contract_chunks (contract_id, chunk_text, embedding, metadata, created_at) " +
                        "VALUES (?, ?, ?::vector, ?::jsonb, NOW())",
                contractId, chunkText, vectorLiteral, metadataJson
        );
    }

    public List<ChunkSearchResult> similaritySearch(float[] queryEmbedding, int topK, Long contractId) {
        String vectorLiteral = toVectorLiteral(queryEmbedding);

        if (contractId == null) {
            String sql = """
                    SELECT cc.id, cc.contract_id, c.name AS contract_name, cc.chunk_text, cc.metadata,
                           1 - (cc.embedding <=> ?::vector) AS similarity
                    FROM contract_chunks cc
                    JOIN contracts c ON c.id = cc.contract_id
                    ORDER BY cc.embedding <=> ?::vector
                    LIMIT ?
                    """;
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapResult(rs),
                    vectorLiteral, vectorLiteral, topK);
        }

        String sql = """
                SELECT cc.id, cc.contract_id, c.name AS contract_name, cc.chunk_text, cc.metadata,
                       1 - (cc.embedding <=> ?::vector) AS similarity
                FROM contract_chunks cc
                JOIN contracts c ON c.id = cc.contract_id
                WHERE cc.contract_id = ?
                ORDER BY cc.embedding <=> ?::vector
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapResult(rs),
                vectorLiteral, contractId, vectorLiteral, topK);
    }

    public void deleteByContractId(Long contractId) {
        jdbcTemplate.update("DELETE FROM contract_chunks WHERE contract_id = ?", contractId);
    }

    private ChunkSearchResult mapResult(ResultSet rs) throws SQLException {
        Map<String, Object> metadata = parseMetadata(rs.getString("metadata"));
        return ChunkSearchResult.builder()
                .id(rs.getLong("id"))
                .contractId(rs.getLong("contract_id"))
                .contractName(rs.getString("contract_name"))
                .chunkText(rs.getString("chunk_text"))
                .metadata(metadata)
                .similarity(rs.getDouble("similarity"))
                .build();
    }

    private Map<String, Object> parseMetadata(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse chunk metadata", e);
            return new HashMap<>();
        }
    }

    private String toJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata != null ? metadata : Map.of());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize metadata", e);
        }
    }

    private String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
