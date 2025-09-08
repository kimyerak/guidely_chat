package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.credits.EndingCreditsResponse;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.Credit;
import com.guidely.chatorchestra.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Service for generating ending credits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EndingCreditsService {
    
    private final ConversationRepository conversationRepository;
    
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
        
        // Create mock credits
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
        
        log.info("Generated credits for conversation: {} - {} messages, {} seconds", 
                conversationId, messageCount, durationSec);
        
        return EndingCreditsResponse.builder()
                .sessionId(conversationId)
                .summary(summary)
                .credits(credits)
                .build();
    }
}




