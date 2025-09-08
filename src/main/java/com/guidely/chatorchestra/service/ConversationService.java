package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.conversation.GetConversationResponse;
import com.guidely.chatorchestra.dto.conversation.PostMessageResponse;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.Message;
import com.guidely.chatorchestra.model.enums.ConversationStatus;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing conversations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    
    private final ConversationRepository conversationRepository;
    
    public Conversation startSession(String userId, java.util.Map<String, Object> metadata) {
        log.info("Starting conversation session for userId: {}", userId);
        
        Conversation conversation = Conversation.builder()
                .sessionId(UUID.randomUUID())
                .userId(userId)
                .status(ConversationStatus.STARTED)
                .startedAt(Instant.now())
                .build();
        
        Conversation saved = conversationRepository.save(conversation);
        log.info("Started conversation session: {}", saved.getSessionId());
        
        return saved;
    }
    
    public PostMessageResponse appendMessage(UUID sessionId, MessageRole role, String content, 
                                           java.util.Map<String, Object> metadata) {
        log.info("Appending message to session: {}, role: {}", sessionId, role);
        
        Conversation conversation = conversationRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + sessionId));
        
        if (conversation.getStatus() == ConversationStatus.ENDED) {
            throw new IllegalStateException("Cannot add message to ended conversation");
        }
        
        // Update status to ACTIVE if it was STARTED
        if (conversation.getStatus() == ConversationStatus.STARTED) {
            conversation.setStatus(ConversationStatus.ACTIVE);
        }
        
        Message message = Message.builder()
                .messageId(UUID.randomUUID())
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createdAt(Instant.now())
                .build();
        
        conversation.getMessages().add(message);
        conversationRepository.save(conversation);
        
        log.info("Added message: {} to session: {}", message.getMessageId(), sessionId);
        
        // Generate mock assistant preview for USER messages
        String assistantPreview = null;
        if (role == MessageRole.USER) {
            assistantPreview = "This is a mock reply to: " + content;
        }
        
        return PostMessageResponse.builder()
                .messageId(message.getMessageId())
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .createdAt(message.getCreatedAt())
                .assistantPreview(assistantPreview)
                .build();
    }
    
    public GetConversationResponse getSession(UUID sessionId, int page, int size) {
        log.info("Getting conversation session: {}, page: {}, size: {}", sessionId, page, size);
        
        Conversation conversation = conversationRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + sessionId));
        
        List<Message> messages = conversation.getMessages();
        Pageable pageable = PageRequest.of(page, size);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), messages.size());
        List<Message> pagedMessages = messages.subList(start, end);
        
        Page<Message> messagePage = new PageImpl<>(pagedMessages, pageable, messages.size());
        
        List<GetConversationResponse.MessageDto> messageDtos = messagePage.getContent().stream()
                .map(msg -> GetConversationResponse.MessageDto.builder()
                        .messageId(msg.getMessageId())
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return GetConversationResponse.builder()
                .sessionId(sessionId)
                .status(conversation.getStatus().name())
                .messages(messageDtos)
                .total(messagePage.getTotalElements())
                .build();
    }
    
    public Conversation endSession(UUID sessionId, String reason) {
        log.info("Ending conversation session: {}, reason: {}", sessionId, reason);
        
        Conversation conversation = conversationRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + sessionId));
        
        conversation.setStatus(ConversationStatus.ENDED);
        conversation.setEndedAt(Instant.now());
        conversation.setReason(reason);
        
        Conversation saved = conversationRepository.save(conversation);
        log.info("Ended conversation session: {}", sessionId);
        
        return saved;
    }
}




