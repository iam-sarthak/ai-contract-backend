package com.contractai.search.web;

import com.contractai.search.application.SearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public SearchService.SearchResponse search(@Valid @RequestBody SearchRequestDto request) {
        SearchService.SearchRequest searchRequest = new SearchService.SearchRequest();
        searchRequest.setQuery(request.getQuery());
        return searchService.search(searchRequest);
    }

    @Getter
    @Setter
    public static class SearchRequestDto {
        @NotBlank
        private String query;
    }
}
