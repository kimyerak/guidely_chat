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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Value;

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
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${rag.server.url:http://localhost:8000}")
    private String ragServerUrl;

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
    
    @PostMapping("/{conversationId}/chat")
    @Operation(summary = "RAG Chat", description = "Process user message with RAG server and store conversation")
    public ResponseEntity<ResponseEnvelope<PostMessageResponse>> ragChat(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Valid @RequestBody PostMessageRequest request,
            @RequestParam(required = false) String character) {
        
        log.info("RAG Chat - conversationId: {}, role: {}, content length: {}, character: {}", 
                conversationId, request.getRole(), request.getContent().length(), character);
        
        // 1. 사용자 메시지를 DB에 저장
        PostMessageResponse userMessage = conversationService.appendMessage(
                conversationId,
                request.getRole().name().toLowerCase(),
                request.getContent(),
                null
        );
        
        try {
            // 2. RAG 서버에 채팅 요청
            String aiResponse = callRagServerForChat(request.getContent(), conversationId, character);
            
            // 3. AI 응답을 DB에 저장
            PostMessageResponse assistantMessage = conversationService.appendMessage(
                    conversationId,
                    "assistant",
                    aiResponse,
                    "RAG server response"
            );
            
            return ResponseEntity.ok(ResponseEnvelope.success(assistantMessage));
            
        } catch (Exception e) {
            log.error("Failed to call RAG server for conversation: {}", conversationId, e);
            
            // 4. RAG 서버 실패 시 Mock 응답으로 Fallback
            String fallbackResponse = generateRagResponse(request.getContent(), character);
            PostMessageResponse assistantMessage = conversationService.appendMessage(
                    conversationId,
                    "assistant", 
                    fallbackResponse,
                    "Fallback response (RAG server unavailable)"
            );
            
            return ResponseEntity.ok(ResponseEnvelope.success(assistantMessage));
        }
    }
    
    /**
     * RAG 서버의 /chat API 호출
     */
    private String callRagServerForChat(String userMessage, Long conversationId, String character) {
        try {
            String url = ragServerUrl + "/chat";
            
            // RAG 서버 요청 객체 생성
            RagChatRequest request = RagChatRequest.builder()
                    .message(userMessage)
                    .sessionId(conversationId)
                    .character(character)
                    .build();
            
            log.info("Calling RAG server for chat: {}", url);
            
            RagChatResponse response = restTemplate.postForObject(url, request, RagChatResponse.class);
            
            if (response != null && response.getResponse() != null) {
                return response.getResponse();
            }
            
            throw new RuntimeException("RAG server returned empty response");
            
        } catch (RestClientException e) {
            log.error("Failed to call RAG server", e);
            throw e;
        }
    }
    
    private String generateRagResponse(String userMessage, String character) {
        // Mock RAG 응답 생성 (Fallback)
        StringBuilder response = new StringBuilder();
        
        if (character != null) {
            response.append(String.format("[%s 캐릭터로 응답] ", character));
        }
        
        response.append("죄송합니다. 현재 AI 서버에 연결할 수 없어 임시 응답을 드립니다. ");
        response.append("사용자의 질문 '").append(userMessage).append("'에 대해 나중에 다시 답변드리겠습니다.");
        
        return response.toString();
    }
    
    // RAG 서버 요청/응답 DTO들
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RagChatRequest {
        private String message;
        private Long sessionId;
        private String character;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RagChatResponse {
        private String response;
        private Long sessionId;
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




