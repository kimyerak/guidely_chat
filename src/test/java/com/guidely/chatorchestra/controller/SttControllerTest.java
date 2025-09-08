package com.guidely.chatorchestra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidely.chatorchestra.dto.stt.SttRequest;
import com.guidely.chatorchestra.dto.stt.SttResponse;
import com.guidely.chatorchestra.service.SttService;
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
 * WebMvcTest for SttController
 */
@WebMvcTest(SttController.class)
class SttControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SttService sttService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void transcribe_ShouldReturn200AndTranscript() throws Exception {
        // Given
        SttRequest request = SttRequest.builder()
                .audioBase64("QUJDRA==")
                .language("ko-KR")
                .build();

        SttResponse response = SttResponse.builder()
                .transcript("Transcribed 4 bytes in ko-KR")
                .durationMs(202)
                .language("ko-KR")
                .build();

        when(sttService.transcribe(any(), any())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/stt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transcript").value("Transcribed 4 bytes in ko-KR"))
                .andExpect(jsonPath("$.data.duration_ms").value(202))
                .andExpect(jsonPath("$.data.language").value("ko-KR"));
    }
}
