package com.group1.career.repository;

import com.group1.career.model.document.ChatSessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSessionDocument, String> {
    List<ChatSessionDocument> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
