package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.tts.TtsResponse;
import com.guidely.chatorchestra.model.enums.VoiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service for Text-to-Speech conversion (mock implementation)
 */
@Service
@Slf4j
public class TtsService {
    
    public TtsResponse synthesize(String text, VoiceType voice, String language) {
        log.info("Synthesizing text: '{}' with voice: {} in language: {}", text, voice, language);
        
        // Generate mock audio as base64 of "AUDIO:<text>"
        String audioContent = "AUDIO:" + text;
        String audioBase64 = Base64.getEncoder().encodeToString(
                audioContent.getBytes(StandardCharsets.UTF_8));
        
        // Calculate estimated duration proportional to text length (roughly 100ms per character)
        long estimatedDurationMs = Math.max(800, text.length() * 100);
        
        log.info("Synthesized audio: {} characters, estimated duration: {}ms", 
                text.length(), estimatedDurationMs);
        
        return TtsResponse.builder()
                .audioBase64(audioBase64)
                .voice(voice)
                .language(language)
                .estimatedDurationMs(estimatedDurationMs)
                .build();
    }
}




