package com.guidely.chatorchestra.dto.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request DTO for starting a conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StartConversationRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
}




