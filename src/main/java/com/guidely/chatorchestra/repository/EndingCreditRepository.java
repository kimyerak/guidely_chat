package com.guidely.chatorchestra.repository;

import com.guidely.chatorchestra.model.EndingCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for EndingCredit entity
 */
@Repository
public interface EndingCreditRepository extends JpaRepository<EndingCredit, Long> {
    
    /**
     * Find ending credits by conversation ID
     */
    List<EndingCredit> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
} 