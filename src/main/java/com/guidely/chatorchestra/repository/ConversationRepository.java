package com.guidely.chatorchestra.repository;

import com.guidely.chatorchestra.model.Conversation;
import com.guidely.chatorchestra.model.enums.ConversationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * JPA Repository for conversations
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    /**
     * Find conversations by user ID
     */
    List<Conversation> findByUserId(Long userId);
}




