package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.tts.TtsRequest;
import com.guidely.chatorchestra.dto.tts.TtsResponse;
import com.guidely.chatorchestra.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Text-to-Speech conversion
 */
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Text-to-Speech", description = "APIs for text-to-speech conversion")
public class TtsController {
    
    private final TtsService ttsService;
    
    @PostMapping
    @Operation(summary = "Convert text to speech", description = "Synthesizes text to audio")
    public ResponseEntity<ResponseEnvelope<TtsResponse>> synthesize(
            @Valid @RequestBody TtsRequest request) {
        
        log.info("Synthesizing text with voice: {} in language: {}", 
                request.getVoice(), request.getLanguage());
        
        TtsResponse response = ttsService.synthesize(
                request.getText(),
                request.getVoice(),
                request.getLanguage()
        );
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




