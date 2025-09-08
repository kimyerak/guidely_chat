package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.conversation.*;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for conversation management
 */
@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Conversation Management", description = "APIs for managing conversations")
public class ConversationController {
    
    private final ConversationService conversationService;
    
    @PostMapping("/start")
    @Operation(summary = "Start a new conversation", description = "Creates a new conversation session")
    public ResponseEntity<ResponseEnvelope<StartConversationResponse>> startConversation(
            @Valid @RequestBody StartConversationRequest request) {
        
        log.info("Starting conversation for userId: {}", request.getUserId());
        
        Conversation conversation = conversationService.startSession(
                request.getUserId(), 
                request.getMetadata()
        );
        
        StartConversationResponse response = StartConversationResponse.builder()
                .sessionId(conversation.getSessionId())
                .status(conversation.getStatus().name())
                .startedAt(conversation.getStartedAt())
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.success(response));
    }
    
    @PostMapping("/{sessionId}/message")
    @Operation(summary = "Post a message", description = "Adds a message to the conversation")
    public ResponseEntity<ResponseEnvelope<PostMessageResponse>> postMessage(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Valid @RequestBody PostMessageRequest request) {
        
        log.info("Posting message to session: {}, role: {}", sessionId, request.getRole());
        
        PostMessageResponse response = conversationService.appendMessage(
                sessionId,
                request.getRole(),
                request.getContent(),
                request.getMetadata()
        );
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
    
    @GetMapping("/{sessionId}")
    @Operation(summary = "Get conversation", description = "Retrieves conversation details with pagination")
    public ResponseEntity<ResponseEnvelope<GetConversationResponse>> getConversation(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size) {
        
        log.info("Getting conversation session: {}, page: {}, size: {}", sessionId, page, size);
        
        GetConversationResponse response = conversationService.getSession(sessionId, page, size);
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
    
    @PostMapping("/{sessionId}/end")
    @Operation(summary = "End conversation", description = "Ends the conversation session")
    public ResponseEntity<ResponseEnvelope<EndConversationResponse>> endConversation(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Valid @RequestBody EndConversationRequest request) {
        
        log.info("Ending conversation session: {}, reason: {}", sessionId, request.getReason());
        
        Conversation conversation = conversationService.endSession(sessionId, request.getReason());
        
        EndConversationResponse response = EndConversationResponse.builder()
                .sessionId(conversation.getSessionId())
                .status(conversation.getStatus().name())
                .endedAt(conversation.getEndedAt())
                .build();
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




