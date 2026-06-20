package com.contractai.document;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextChunker {

    private static final int CHUNK_SIZE = 800;
    private static final int CHUNK_OVERLAP = 100;

    public List<String> chunk(String text) {
        DocumentSplitter splitter = DocumentSplitters.recursive(CHUNK_SIZE, CHUNK_OVERLAP);
        Document document = Document.from(text);
        List<TextSegment> segments = splitter.split(document);
        return segments.stream()
                .map(TextSegment::text)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
