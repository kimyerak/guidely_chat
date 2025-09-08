package com.guidely.chatorchestra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidely.chatorchestra.dto.tts.TtsRequest;
import com.guidely.chatorchestra.dto.tts.TtsResponse;
import com.guidely.chatorchestra.model.enums.VoiceType;
import com.guidely.chatorchestra.service.TtsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest for TtsController
 */
@WebMvcTest(TtsController.class)
class TtsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TtsService ttsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void synthesize_ShouldReturn200AndAudioBase64() throws Exception {
        // Given
        TtsRequest request = TtsRequest.builder()
                .text("안녕하세요")
                .voice(VoiceType.NEUTRAL)
                .language("ko-KR")
                .build();

        TtsResponse response = TtsResponse.builder()
                .audioBase64("QVVESU867J207Iqk7Yq47ZWY7Iqk")
                .voice(VoiceType.NEUTRAL)
                .language("ko-KR")
                .estimatedDurationMs(500)
                .build();

        when(ttsService.synthesize(any(), any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/tts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.audio_base64").value("QVVESU867J207Iqk7Yq47ZWY7Iqk"))
                .andExpect(jsonPath("$.data.voice").value("NEUTRAL"))
                .andExpect(jsonPath("$.data.language").value("ko-KR"))
                .andExpect(jsonPath("$.data.estimated_duration_ms").value(500));
    }
}
