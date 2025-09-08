package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.stt.SttResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Service for Speech-to-Text conversion (mock implementation)
 */
@Service
@Slf4j
public class SttService {
    
    public SttResponse transcribe(String audioBase64, String language) {
        log.info("Transcribing audio in language: {}", language);
        
        try {
            // Decode the base64 audio to get the length
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            int audioLength = audioBytes.length;
            
            // Calculate mock duration: min(3000, 200 + audioLen/2)
            long durationMs = Math.min(3000, 200 + audioLength / 2);
            
            // Generate mock transcript
            String transcript = String.format("Transcribed %d bytes in %s", audioLength, language);
            
            log.info("Transcribed audio: {} bytes, duration: {}ms", audioLength, durationMs);
            
            return SttResponse.builder()
                    .transcript(transcript)
                    .durationMs(durationMs)
                    .language(language)
                    .build();
                    
        } catch (IllegalArgumentException e) {
            log.error("Invalid base64 audio data", e);
            throw new IllegalArgumentException("Invalid base64 audio data", e);
        }
    }
}




