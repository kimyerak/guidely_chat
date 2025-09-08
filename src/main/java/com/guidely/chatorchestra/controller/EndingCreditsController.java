package com.guidely.chatorchestra.controller;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import com.guidely.chatorchestra.dto.credits.EndingCreditsRequest;
import com.guidely.chatorchestra.dto.credits.EndingCreditsResponse;
import com.guidely.chatorchestra.service.EndingCreditsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for ending credits
 */
@RestController
@RequestMapping("/api/ending-credits")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Ending Credits", description = "APIs for generating ending credits")
public class EndingCreditsController {
    
    private final EndingCreditsService endingCreditsService;
    
    @PostMapping
    @Operation(summary = "Generate ending credits", description = "Generates ending credits for a conversation")
    public ResponseEntity<ResponseEnvelope<EndingCreditsResponse>> generateCredits(
            @Valid @RequestBody EndingCreditsRequest request) {
        
        log.info("Generating ending credits for session: {}, includeDuration: {}", 
                request.getSessionId(), request.isIncludeDuration());
        
        EndingCreditsResponse response = endingCreditsService.generateCredits(
                request.getSessionId(),
                request.isIncludeDuration()
        );
        
        return ResponseEntity.ok(ResponseEnvelope.success(response));
    }
}




