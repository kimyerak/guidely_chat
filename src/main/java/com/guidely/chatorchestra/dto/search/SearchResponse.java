package com.guidely.chatorchestra.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.guidely.chatorchestra.model.SearchResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for search index query
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private String query;
    private List<SearchResult> results;
}




