package com.guidely.chatorchestra.dto.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.guidely.chatorchestra.model.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for posting a message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostMessageResponse {
    private UUID messageId;
    private UUID sessionId;
    private MessageRole role;
    private String content;
    private Instant createdAt;
    private String assistantPreview;
}




