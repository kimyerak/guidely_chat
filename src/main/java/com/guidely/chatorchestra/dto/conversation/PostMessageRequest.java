package com.guidely.chatorchestra.dto.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.guidely.chatorchestra.model.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * Request DTO for posting a message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostMessageRequest {
    @NotNull(message = "Role is required")
    private MessageRole role;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private Map<String, Object> metadata;
}




