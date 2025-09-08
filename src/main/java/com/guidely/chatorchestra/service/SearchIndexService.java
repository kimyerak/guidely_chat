package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.search.SearchResponse;
import com.guidely.chatorchestra.model.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for search index integration (mock implementation)
 */
@Service
@Slf4j
public class SearchIndexService {
    
    public SearchResponse query(String query, int topK, java.util.Map<String, Object> filters, UUID sessionId) {
        log.info("Searching for query: '{}' with topK: {}, sessionId: {}", query, topK, sessionId);
        
        List<SearchResult> results = new ArrayList<>();
        
        // Generate mock results with fake scores and snippets
        for (int i = 0; i < topK; i++) {
            double score = 0.9 - (i * 0.05); // Decreasing scores
            String snippet = String.format("Mock snippet about '%s' - result %d", query, i + 1);
            
            SearchResult result = SearchResult.builder()
                    .id("doc-" + (i + 1))
                    .score(score)
                    .snippet(snippet)
                    .build();
            
            results.add(result);
        }
        
        log.info("Found {} mock results for query: '{}'", results.size(), query);
        
        return SearchResponse.builder()
                .query(query)
                .results(results)
                .build();
    }
}




