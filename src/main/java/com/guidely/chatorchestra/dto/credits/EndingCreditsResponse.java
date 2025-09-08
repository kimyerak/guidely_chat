package com.guidely.chatorchestra.dto.credits;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.guidely.chatorchestra.model.Credit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for ending credits
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EndingCreditsResponse {
    private Long sessionId;
    private SummaryDto summary;
    private List<Credit> credits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SummaryDto {
        private int messages;
        private long durationSec;
    }
}




