package com.guidely.chatorchestra.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for search index query
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchRequest {
    @NotBlank(message = "Query is required")
    private String query;
    
    @Builder.Default
    @Min(value = 1, message = "TopK must be at least 1")
    @Max(value = 50, message = "TopK must be at most 50")
    private int topK = 5;
    
    private Map<String, Object> filters;
    private UUID sessionId;
}




