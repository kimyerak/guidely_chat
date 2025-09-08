package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.search.SearchRequest;
import com.guidely.chatorchestra.dto.search.SearchResponse;
import com.guidely.chatorchestra.service.SearchIndexService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for search index integration
 */
@RestController
@RequestMapping("/api/search-index")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Search Index", description = "APIs for search index integration (RAG Gateway)")
public class SearchIndexController {
    
    private final SearchIndexService searchIndexService;
    
    @PostMapping("/query")
    @Operation(summary = "Query search index", description = "Searches the index with the given query")
    public ResponseEntity<ResponseEnvelope<SearchResponse>> query(
            @Valid @RequestBody SearchRequest request) {
        
        log.info("Searching for query: '{}' with topK: {}", request.getQuery(), request.getTopK());
        
        SearchResponse response = searchIndexService.query(
                request.getQuery(),
                request.getTopK(),
                request.getFilters(),
                request.getSessionId()
        );
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




