package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.conversation.GetConversationResponse;
import com.guidely.chatorchestra.dto.conversation.PostMessageResponse;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.Message;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ConversationService {
    
    private final ConversationRepository conversationRepository;
    
    public Conversation startSession(String userIdStr, java.util.Map<String, Object> metadata) {
        Long userId = Long.parseLong(userIdStr);
        log.info("Starting conversation session for userId: {}", userId);
        
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .startedAt(LocalDateTime.now())
                .build();
        
        Conversation saved = conversationRepository.save(conversation);
        log.info("Started conversation session: {}", saved.getId());
        
        return saved;
    }
    
    public PostMessageResponse appendMessage(Long conversationId, String sender, String content, 
                                             String assistantPreview) {
        log.info("Appending message to conversation: {}, sender: {}, content length: {}", 
                conversationId, sender, content.length());
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        
        Message message = Message.builder()
                .sender(sender)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        
        conversation.addMessage(message);
        conversationRepository.save(conversation);
        
        log.info("Added message: {} to conversation: {}", message.getId(), conversationId);
        
        return PostMessageResponse.builder()
                .messageId(message.getId())
                .sessionId(conversationId)
                .role(MessageRole.valueOf(sender.toUpperCase())) // 임시 변환
                .content(content)
                .createdAt(message.getCreatedAt().atZone(ZoneOffset.UTC).toInstant())
                .assistantPreview(assistantPreview)
                .build();
    }
    
    public GetConversationResponse getSession(Long conversationId, int page, int size) {
        log.info("Getting conversation session: {}, page: {}, size: {}", conversationId, page, size);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        
        List<Message> messages = conversation.getMessages();
        Pageable pageable = PageRequest.of(page, size);
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), messages.size());
        List<Message> pagedMessages = messages.subList(start, end);
        
        Page<Message> messagePage = new PageImpl<>(pagedMessages, pageable, messages.size());
        
        List<GetConversationResponse.MessageDto> messageDtos = messagePage.getContent().stream()
                .map(msg -> GetConversationResponse.MessageDto.builder()
                        .messageId(msg.getId())
                        .role(MessageRole.valueOf(msg.getSender().toUpperCase())) // 임시 변환
                        .content(msg.getContent())
                        .createdAt(msg.getCreatedAt().atZone(ZoneOffset.UTC).toInstant())
                        .build())
                .collect(Collectors.toList());
        
        return GetConversationResponse.builder()
                .sessionId(conversationId)
                .status("ACTIVE") // 임시 하드코딩
                .messages(messageDtos)
                .total(messagePage.getTotalElements())
                .build();
    }
    
    public Conversation endSession(Long conversationId, String reason) {
        log.info("Ending conversation session: {}, reason: {}", conversationId, reason);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        
        conversation.endConversation();
        Conversation saved = conversationRepository.save(conversation);
        
        log.info("Ended conversation session: {}", conversationId);
        return saved;
    }
}




