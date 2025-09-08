package com.guidely.chatorchestra.repository;

import com.guidely.chatorchestra.model.Conversation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for conversations using ConcurrentHashMap
 */
@Repository
public class ConversationRepository {
    
    private final Map<UUID, Conversation> conversations = new ConcurrentHashMap<>();
    
    public Conversation save(Conversation conversation) {
        conversations.put(conversation.getSessionId(), conversation);
        return conversation;
    }
    
    public Optional<Conversation> findById(UUID sessionId) {
        return Optional.ofNullable(conversations.get(sessionId));
    }
    
    public List<Conversation> findAll() {
        return List.copyOf(conversations.values());
    }
    
    public void deleteById(UUID sessionId) {
        conversations.remove(sessionId);
    }
    
    public boolean existsById(UUID sessionId) {
        return conversations.containsKey(sessionId);
    }
    
    public long count() {
        return conversations.size();
    }
}




