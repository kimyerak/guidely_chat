package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.credits.EndingCreditsResponse;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.Credit;
import com.guidely.chatorchestra.model.EndingCredit;
import com.guidely.chatorchestra.model.Message;
import com.guidely.chatorchestra.repository.ConversationRepository;
import com.guidely.chatorchestra.repository.EndingCreditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 * Service for generating ending credits
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EndingCreditsService {
    
    private final ConversationRepository conversationRepository;
    private final EndingCreditRepository endingCreditRepository;
    private final RestTemplate restTemplate;
    
    @Value("${rag.server.url:http://localhost:8080}")
    private String ragServerUrl;
    
    @Value("${rag.server.enabled:false}")
    private boolean ragServerEnabled;
    
    public EndingCreditsResponse generateCredits(Long conversationId, boolean includeDuration) {
        log.info("Generating ending credits for conversation: {}, includeDuration: {}", conversationId, includeDuration);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        
        // Calculate message count
        int messageCount = conversation.getMessages().size();
        
        // Calculate duration
        long durationSec = 0;
        if (includeDuration) {
            LocalDateTime endTime = conversation.getEndedAt() != null ? 
                    conversation.getEndedAt() : LocalDateTime.now();
            durationSec = endTime.atZone(ZoneOffset.UTC).toEpochSecond() - 
                         conversation.getStartedAt().atZone(ZoneOffset.UTC).toEpochSecond();
        }
        
        // 🔥 NEW: 감성적인 요약 10줄 생성 및 DB 저장
        List<String> summaries = generateConversationSummaries(conversation);
        List<EndingCredit> savedCredits = saveEndingCreditsToDb(conversationId, summaries);
        
        // Create mock credits (기존 로직 유지)
        List<Credit> credits = Arrays.asList(
                Credit.builder()
                        .role("User")
                        .name("You")
                        .build(),
                Credit.builder()
                        .role("Assistant")
                        .name("Chat-Orchestra")
                        .build()
        );
        
        EndingCreditsResponse.SummaryDto summary = EndingCreditsResponse.SummaryDto.builder()
                .messages(messageCount)
                .durationSec(durationSec)
                .build();
        
        log.info("Generated credits for conversation: {} - {} messages, {} seconds, {} summaries saved", 
                conversationId, messageCount, durationSec, savedCredits.size());
        
        return EndingCreditsResponse.builder()
                .sessionId(conversationId)
                .summary(summary)
                .credits(credits)
                .summaries(summaries) // 🔥 NEW: 요약 10줄 추가
                .build();
    }
    
    /**
     * 기존에 저장된 엔딩크레딧을 조회
     */
    public EndingCreditsResponse getExistingCredits(Long conversationId, boolean includeDuration) {
        log.info("Getting existing ending credits for conversation: {}, includeDuration: {}", conversationId, includeDuration);
        
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        
        // DB에서 기존 엔딩크레딧 조회
        List<EndingCredit> existingCredits = endingCreditRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        
        if (existingCredits.isEmpty()) {
            log.warn("No ending credits found for conversation: {}. Generating new ones...", conversationId);
            return generateCredits(conversationId, includeDuration);
        }
        
        // 기존 크레딧들을 String 리스트로 변환
        List<String> summaries = existingCredits.stream()
                .map(EndingCredit::getContent)
                .collect(Collectors.toList());
        
        // Calculate message count
        int messageCount = conversation.getMessages().size();
        
        // Calculate duration
        long durationSec = 0;
        if (includeDuration && conversation.getStartedAt() != null) {
            LocalDateTime endTime = conversation.getEndedAt() != null ? 
                    conversation.getEndedAt() : LocalDateTime.now();
            durationSec = endTime.atZone(ZoneOffset.UTC).toEpochSecond() - 
                         conversation.getStartedAt().atZone(ZoneOffset.UTC).toEpochSecond();
        }
        
        // Create mock credits (기존 로직 유지)
        List<Credit> credits = Arrays.asList(
                Credit.builder()
                        .role("User")
                        .name("You")
                        .build(),
                Credit.builder()
                        .role("Assistant")
                        .name("Chat-Orchestra")
                        .build()
        );
        
        EndingCreditsResponse.SummaryDto summary = EndingCreditsResponse.SummaryDto.builder()
                .messages(messageCount)
                .durationSec(durationSec)
                .build();
        
        log.info("Retrieved {} existing credits for conversation: {} - {} messages, {} seconds", 
                existingCredits.size(), conversationId, messageCount, durationSec);
        
        return EndingCreditsResponse.builder()
                .sessionId(conversationId)
                .summary(summary)
                .credits(credits)
                .summaries(summaries)
                .build();
    }
    
    /**
     * 대화 내용을 바탕으로 감성적인 요약 10줄 생성
     */
    private List<String> generateConversationSummaries(Conversation conversation) {
        List<String> summaries = new ArrayList<>();
        List<Message> messages = conversation.getMessages();
        
        if (messages.isEmpty()) {
            // 메시지가 없을 경우 기본 요약
            summaries.add("새로운 대화가 시작되었어");
            summaries.add("아직 많은 이야기를 나누지 못했지만");
            summaries.add("이것이 우리의 첫 만남이었어");
            return summaries;
        }
        
        // 🔥 NEW: 실제 RAG 서버 호출 시도
        if (ragServerEnabled) {
            try {
                summaries = callRagServerForSummary(conversation);
                if (!summaries.isEmpty()) {
                    log.info("Successfully generated summaries from RAG server for conversation: {}", conversation.getId());
                    return summaries;
                }
            } catch (Exception e) {
                log.warn("Failed to call RAG server for conversation: {}, falling back to mock", conversation.getId(), e);
            }
        }
        
        // Fallback: Mock 감성적 요약 생성
        log.info("Using mock summary generation for conversation: {}", conversation.getId());
        summaries.add("우리의 대화가 " + messages.size() + "번의 메시지로 이어졌어");
        summaries.add("첫 번째 질문부터 마지막 답변까지");
        summaries.add("서로의 마음을 조금씩 알아가는 시간이었어");
        summaries.add("때로는 진지하게, 때로는 유쾌하게");
        summaries.add("질문과 답변 사이에 숨겨진 이야기들");
        summaries.add("AI와 인간이 만나는 특별한 순간");
        summaries.add("기술 너머로 전해지는 따뜻함");
        summaries.add("디지털 공간에서 나눈 진짜 소통");
        summaries.add("이 대화가 누군가에게는 작은 위로가 되길");
        summaries.add("다음에 또 만날 수 있기를 바라며");
        
        return summaries;
    }
    
    /**
     * RAG 서버에 요약 생성 요청
     */
    private List<String> callRagServerForSummary(Conversation conversation) {
        try {
            // 대화 내용을 RAG 서버 형식으로 변환
            RagSummaryRequest request = buildRagSummaryRequest(conversation);
            
            String url = ragServerUrl + "/conversation/summarize";
            log.info("Calling RAG server for summary: {}", url);
            
            RagSummaryResponse response = restTemplate.postForObject(url, request, RagSummaryResponse.class);
            
            if (response != null && response.getSummaries() != null && !response.getSummaries().isEmpty()) {
                return response.getSummaries();
            }
            
            log.warn("RAG server returned empty summaries for conversation: {}", conversation.getId());
            return new ArrayList<>();
            
        } catch (RestClientException e) {
            log.error("Failed to call RAG server for conversation: {}", conversation.getId(), e);
            throw e;
        }
    }
    
    /**
     * RAG 서버 요청 객체 생성
     */
    private RagSummaryRequest buildRagSummaryRequest(Conversation conversation) {
        List<RagSummaryRequest.MessageDto> messageDtos = conversation.getMessages().stream()
                .map(msg -> RagSummaryRequest.MessageDto.builder()
                        .role(msg.getSpeaker())
                        .content(msg.getContent())
                        .build())
                .collect(Collectors.toList());
        
        return RagSummaryRequest.builder()
                .sessionId(conversation.getId())
                .messages(messageDtos)
                .count(10) // 요약 10줄 요청
                .build();
    }
    
    // RAG 서버 요청/응답 DTO들
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RagSummaryRequest {
        private Long sessionId;
        private List<MessageDto> messages;
        private int count;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class MessageDto {
            private String role;
            private String content;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RagSummaryResponse {
        private Long sessionId;
        private int totalMessages;
        private List<String> summaries;
    }
    
    /**
     * 생성된 요약들을 ending_credits 테이블에 저장
     */
    private List<EndingCredit> saveEndingCreditsToDb(Long conversationId, List<String> summaries) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + conversationId));
        
        List<EndingCredit> credits = new ArrayList<>();
        
        for (String summary : summaries) {
            EndingCredit credit = EndingCredit.builder()
                    .conversation(conversation)
                    .content(summary)
                    .createdAt(LocalDateTime.now())
                    .build();
            credits.add(credit);
        }
        
        // 🔥 실제 DB 저장
        List<EndingCredit> savedCredits = endingCreditRepository.saveAll(credits);
        log.info("Saved {} ending credits to DB for conversation: {}", savedCredits.size(), conversationId);
        
        return savedCredits;
    }
}




