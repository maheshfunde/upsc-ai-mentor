package com.upscmentor.repository;

import com.upscmentor.model.entity.ChatHistory;
import com.upscmentor.model.enums.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<ChatHistory> findByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, String sessionId);
    List<ChatHistory> findByUserIdAndSubjectOrderByCreatedAtDesc(Long userId, Subject subject);
    List<ChatHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteBySessionId(String sessionId);
}
