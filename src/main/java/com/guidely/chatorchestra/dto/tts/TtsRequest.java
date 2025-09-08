package com.guidely.chatorchestra.dto.tts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.guidely.chatorchestra.model.enums.VoiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Text-to-Speech conversion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TtsRequest {
    @NotBlank(message = "Text is required")
    private String text;
    
    @Builder.Default
    private VoiceType voice = VoiceType.NEUTRAL;
    
    @Builder.Default
    private String language = "ko-KR";
}




