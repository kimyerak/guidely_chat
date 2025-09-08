package com.guidely.chatorchestra.service;

import com.guidely.chatorchestra.dto.credits.EndingCreditsResponse;
import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.Credit;
import com.guidely.chatorchestra.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service for generating ending credits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EndingCreditsService {
    
    private final ConversationRepository conversationRepository;
    
    public EndingCreditsResponse generateCredits(UUID sessionId, boolean includeDuration) {
        log.info("Generating ending credits for session: {}, includeDuration: {}", sessionId, includeDuration);
        
        Conversation conversation = conversationRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found: " + sessionId));
        
        // Calculate message count
        int messageCount = conversation.getMessages().size();
        
        // Calculate duration
        long durationSec = 0;
        if (includeDuration) {
            Instant endTime = conversation.getEndedAt() != null ? 
                    conversation.getEndedAt() : Instant.now();
            durationSec = endTime.getEpochSecond() - conversation.getStartedAt().getEpochSecond();
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
        
        log.info("Generated credits for session: {} - {} messages, {} seconds", 
                sessionId, messageCount, durationSec);
        
        return EndingCreditsResponse.builder()
                .sessionId(sessionId)
                .summary(summary)
                .credits(credits)
                .build();
    }
}




