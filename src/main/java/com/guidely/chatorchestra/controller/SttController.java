package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.stt.SttRequest;
import com.guidely.chatorchestra.dto.stt.SttResponse;
import com.guidely.chatorchestra.service.SttService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Speech-to-Text conversion
 */
@RestController
@RequestMapping("/api/stt")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Speech-to-Text", description = "APIs for speech-to-text conversion")
public class SttController {
    
    private final SttService sttService;
    
    @PostMapping
    @Operation(summary = "Convert speech to text", description = "Transcribes audio to text")
    public ResponseEntity<ResponseEnvelope<SttResponse>> transcribe(
            @Valid @RequestBody SttRequest request) {
        
        log.info("Transcribing audio in language: {}", request.getLanguage());
        
        SttResponse response = sttService.transcribe(
                request.getAudioBase64(),
                request.getLanguage()
        );
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




