package com.guidely.chatorchestra.dto.credits;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for ending credits
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EndingCreditsRequest {
    @NotNull(message = "Session ID is required")
    private UUID sessionId;
    
    @Builder.Default
    private boolean includeDuration = true;
}




