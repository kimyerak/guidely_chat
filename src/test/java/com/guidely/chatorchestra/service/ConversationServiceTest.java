package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.Message;
import com.guidely.chatorchestra.model.enums.ConversationStatus;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.repository.ConversationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ConversationService
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    private ConversationService conversationService;

    @BeforeEach
    void setUp() {
        conversationService = new ConversationService(conversationRepository);
    }

    @Test
    void startSession_ShouldCreateNewConversation() {
        // Given
        String userId = "test-user";
        Conversation savedConversation = Conversation.builder()
                .sessionId(UUID.randomUUID())
                .userId(userId)
                .status(ConversationStatus.STARTED)
                .startedAt(Instant.now())
                .build();

        when(conversationRepository.save(any(Conversation.class))).thenReturn(savedConversation);

        // When
        Conversation result = conversationService.startSession(userId, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getStatus()).isEqualTo(ConversationStatus.STARTED);
        assertThat(result.getStartedAt()).isNotNull();
    }

    @Test
    void appendMessage_ShouldAddMessageToConversation() {
        // Given
        UUID sessionId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .sessionId(sessionId)
                .userId("test-user")
                .status(ConversationStatus.STARTED)
                .startedAt(Instant.now())
                .build();

        when(conversationRepository.findById(sessionId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // When
        var result = conversationService.appendMessage(sessionId, MessageRole.USER, "Hello", null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(sessionId);
        assertThat(result.getRole()).isEqualTo(MessageRole.USER);
        assertThat(result.getContent()).isEqualTo("Hello");
        assertThat(result.getAssistantPreview()).isEqualTo("This is a mock reply to: Hello");
    }

    @Test
    void appendMessage_ShouldThrowExceptionWhenConversationNotFound() {
        // Given
        UUID sessionId = UUID.randomUUID();
        when(conversationRepository.findById(sessionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> conversationService.appendMessage(sessionId, MessageRole.USER, "Hello", null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Conversation not found: " + sessionId);
    }

    @Test
    void endSession_ShouldEndConversation() {
        // Given
        UUID sessionId = UUID.randomUUID();
        Conversation conversation = Conversation.builder()
                .sessionId(sessionId)
                .userId("test-user")
                .status(ConversationStatus.ACTIVE)
                .startedAt(Instant.now())
                .build();

        when(conversationRepository.findById(sessionId)).thenReturn(Optional.of(conversation));
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        // When
        Conversation result = conversationService.endSession(sessionId, "User ended conversation");

        // Then
        assertThat(result.getStatus()).isEqualTo(ConversationStatus.ENDED);
        assertThat(result.getEndedAt()).isNotNull();
        assertThat(result.getReason()).isEqualTo("User ended conversation");
    }
}




