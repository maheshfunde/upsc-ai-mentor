package com.upscmentor.repository;

import com.upscmentor.model.entity.ChatSessionMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatSessionMetaRepository extends JpaRepository<ChatSessionMeta, Long> {

    Optional<ChatSessionMeta> findBySessionId(String sessionId);

    void deleteBySessionId(String sessionId);
}
