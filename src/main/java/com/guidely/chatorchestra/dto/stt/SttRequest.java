package com.guidely.chatorchestra.dto.stt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for Speech-to-Text conversion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SttRequest {
    @NotBlank(message = "Audio base64 is required")
    private String audioBase64;
    
    @Builder.Default
    private String language = "ko-KR";
}




