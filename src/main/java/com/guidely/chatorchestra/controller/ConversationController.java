package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.conversation.*;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.service.ConversationService; // Added import
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


import java.time.ZoneOffset;

/**
 * REST controller for conversation management
 */
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Conversation", description = "Conversation management endpoints")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(summary = "Start a new conversation", description = "Creates a new conversation session")
    public ResponseEntity<ResponseEnvelope<StartConversationResponse>> startConversation() {
        
        log.info("Starting new conversation");
        
        Conversation conversation = conversationService.startSession();
        
        StartConversationResponse response = StartConversationResponse.builder()
                .sessionId(conversation.getId())
                .status("CREATED") // 처음 생성 시 상태
                .startedAt(conversation.getStartedAt() != null ? 
                    conversation.getStartedAt().atZone(ZoneOffset.UTC).toInstant() : null)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseEnvelope.success(response));
    }
    
    @PostMapping("/{conversationId}/messages")
    @Operation(summary = "Post a message", description = "Adds a message to the conversation")
    public ResponseEntity<ResponseEnvelope<PostMessageResponse>> postMessage(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Valid @RequestBody PostMessageRequest request) {
        
        log.info("Posting message to conversation: {}, role: {}", conversationId, request.getRole());
        
        // MessageRole을 String으로 변환 (ERD에서는 speaker가 varchar)
        String speaker = request.getRole().name().toLowerCase();
        
        PostMessageResponse response = conversationService.appendMessage(
                conversationId,
                speaker,
                request.getContent(),
                "Mock assistant preview" // 임시
        );
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
    

    


    @GetMapping("/{conversationId}")
    @Operation(summary = "Get conversation", description = "Retrieves conversation details with messages")
    public ResponseEntity<ResponseEnvelope<GetConversationResponse>> getConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting conversation: {}, page: {}, size: {}", conversationId, page, size);
        
        GetConversationResponse response = conversationService.getSession(conversationId, page, size);
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
    
    @PutMapping("/{conversationId}/end")
    @Operation(summary = "End conversation", description = "Ends the conversation session")
    public ResponseEntity<ResponseEnvelope<EndConversationResponse>> endConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Valid @RequestBody EndConversationRequest request) {
        
        log.info("Ending conversation: {}, reason: {}", conversationId, request.getReason());
        
        // 대화 종료 처리 (ended_at 시간만 업데이트)
        Conversation conversation = conversationService.endSession(conversationId, request.getReason());
        
        // 엔딩크레딧은 클라이언트가 RAG 서버에 직접 요청
        log.info("Conversation ended. Client should call RAG server directly for ending credits: POST /conversation/summarize");
        
        EndConversationResponse response = EndConversationResponse.builder()
                .sessionId(conversation.getId())
                .status("ENDED")
                .endedAt(conversation.getEndedAt().atZone(ZoneOffset.UTC).toInstant())
                .build();
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




