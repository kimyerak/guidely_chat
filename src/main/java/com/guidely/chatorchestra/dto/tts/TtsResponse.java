package com.guidely.chatorchestra.dto.tts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.guidely.chatorchestra.model.enums.VoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Text-to-Speech conversion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TtsResponse {
    private String audioBase64;
    private VoiceType voice;
    private String language;
    private long estimatedDurationMs;
}




