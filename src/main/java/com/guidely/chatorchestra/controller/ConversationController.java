package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.conversation.*;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.enums.MessageRole;
import com.guidely.chatorchestra.service.ConversationService;
import com.guidely.chatorchestra.service.SearchIndexService;
import com.guidely.chatorchestra.service.EndingCreditsService; // Added import
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
import java.util.UUID; // Added import
import com.guidely.chatorchestra.dto.search.SearchResponse;

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
    private final SearchIndexService searchIndexService;
    private final EndingCreditsService endingCreditsService;

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
    @Operation(summary = "RAG Chat", description = "Process user message with RAG and return AI response")
    public ResponseEntity<ResponseEnvelope<PostMessageResponse>> ragChat(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Valid @RequestBody PostMessageRequest request,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(required = false) String character) {
        
        log.info("RAG Chat - conversationId: {}, role: {}, content length: {}, topK: {}, character: {}", 
                conversationId, request.getRole(), request.getContent().length(), topK, character);
        
        // 1. 먼저 사용자 메시지를 DB에 저장
        PostMessageResponse userMessage = conversationService.appendMessage(
                conversationId,
                request.getRole().name().toLowerCase(),
                request.getContent(),
                null
        );
        
        // 2. RAG 검색 수행 (Search Index Service 활용)
        SearchResponse searchResults = searchIndexService.query(
                request.getContent(),
                topK,
                null,
                null // sessionId는 UUID가 아니므로 null로 전달
        );
        
        // 3. RAG 응답 생성 (현재는 Mock)
        String ragResponse = generateRagResponse(request.getContent(), searchResults, character);
        
        // 4. Assistant 응답을 DB에 저장
        PostMessageResponse assistantMessage = conversationService.appendMessage(
                conversationId,
                "assistant",
                ragResponse,
                "RAG response with " + searchResults.getResults().size() + " references"
        );
        
        // 5. 검색 결과를 응답에 포함 (로그로 표시)
        log.info("RAG response generated with {} search results", searchResults.getResults().size());
        
        return ResponseEntity.ok(ResponseEnvelope.success(assistantMessage));
    }
    
    private String generateRagResponse(String userMessage, SearchResponse searchResults, String character) {
        // Mock RAG 응답 생성
        StringBuilder response = new StringBuilder();
        
        if (character != null) {
            response.append(String.format("[%s 캐릭터로 응답] ", character));
        }
        
        response.append("사용자의 질문 '").append(userMessage).append("'에 대해 ");
        response.append(searchResults.getResults().size()).append("개의 관련 문서를 찾았습니다. ");
        
        // 검색 결과 기반 응답 생성
        if (!searchResults.getResults().isEmpty()) {
            response.append("주요 내용: ");
            searchResults.getResults().stream()
                    .limit(2)
                    .forEach(result -> response.append(result.getSnippet()).append(" "));
        }
        
        return response.toString();
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
    @Operation(summary = "End conversation", description = "Ends the conversation session and auto-generates ending credits")
    public ResponseEntity<ResponseEnvelope<EndConversationResponse>> endConversation(
            @Parameter(description = "Conversation ID") @PathVariable Long conversationId,
            @Valid @RequestBody EndConversationRequest request) {
        
        log.info("Ending conversation: {}, reason: {}", conversationId, request.getReason());
        
        // 1. 대화 종료 처리
        Conversation conversation = conversationService.endSession(conversationId, request.getReason());
        
        // 2. 🔥 NEW: 자동으로 엔딩크레딧 생성 (비동기로 처리 가능)
        try {
            log.info("Auto-generating ending credits for conversation: {}", conversationId);
            endingCreditsService.generateCredits(conversationId, true);
            log.info("Successfully generated ending credits for conversation: {}", conversationId);
        } catch (Exception e) {
            log.error("Failed to generate ending credits for conversation: {}", conversationId, e);
            // 엔딩크레딧 생성 실패해도 대화 종료는 성공으로 처리
        }
        
        EndConversationResponse response = EndConversationResponse.builder()
                .sessionId(conversation.getId())
                .status("ENDED") // 하드코딩
                .endedAt(conversation.getEndedAt().atZone(ZoneOffset.UTC).toInstant())
                .build();
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




