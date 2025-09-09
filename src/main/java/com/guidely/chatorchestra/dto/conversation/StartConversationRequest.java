package com.guidely.chatorchestra.dto.conversation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for starting a conversation
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StartConversationRequest {
    // 빈 요청 - conversation ID만 생성
}




