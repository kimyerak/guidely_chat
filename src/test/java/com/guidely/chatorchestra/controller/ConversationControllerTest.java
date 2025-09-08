package com.guidely.chatorchestra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guidely.chatorchestra.dto.conversation.StartConversationRequest;
import com.guidely.chatorchestra.dto.conversation.PostMessageRequest;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.service.ConversationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WebMvcTest for ConversationController
 */
@WebMvcTest(ConversationController.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startConversation_ShouldReturn201AndSessionId() throws Exception {
        // Given
        StartConversationRequest request = StartConversationRequest.builder()
                .userId("test-user")
                .build();

        UUID sessionId = UUID.randomUUID();
        when(conversationService.startSession(any(), any()))
                .thenReturn(com.guidely.chatorchestra.model.Conversation.builder()
                        .sessionId(sessionId)
                        .userId("test-user")
                        .status(com.guidely.chatorchestra.model.enums.ConversationStatus.STARTED)
                        .startedAt(java.time.Instant.now())
                        .build());

        // When & Then
        mockMvc.perform(post("/api/conversation/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.session_id").value(sessionId.toString()))
                .andExpect(jsonPath("$.data.status").value("STARTED"));
    }

    @Test
    void postMessage_ShouldReturn200AndAssistantPreview() throws Exception {
        // Given
        UUID sessionId = UUID.randomUUID();
        PostMessageRequest request = PostMessageRequest.builder()
                .role(MessageRole.USER)
                .content("안녕")
                .build();

        when(conversationService.appendMessage(eq(sessionId), any(), any(), any()))
                .thenReturn(com.guidely.chatorchestra.dto.conversation.PostMessageResponse.builder()
                        .messageId(UUID.randomUUID())
                        .sessionId(sessionId)
                        .role(MessageRole.USER)
                        .content("안녕")
                        .createdAt(java.time.Instant.now())
                        .assistantPreview("This is a mock reply to: 안녕")
                        .build());

        // When & Then
        mockMvc.perform(post("/api/conversation/{sessionId}/message", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assistant_preview").value("This is a mock reply to: 안녕"));
    }
}
