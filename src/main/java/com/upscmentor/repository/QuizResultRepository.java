package com.upscmentor.repository;

import com.upscmentor.model.entity.QuizResult;
import com.upscmentor.model.enums.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<QuizResult> findByUserIdAndSubjectOrderByCreatedAtDesc(Long userId, Subject subject);

    @Query("SELECT AVG(q.scorePercentage) FROM QuizResult q WHERE q.userId = :userId")
    Double findAverageScoreByUserId(Long userId);

    @Query("SELECT AVG(q.scorePercentage) FROM QuizResult q WHERE q.userId = :userId AND q.subject = :subject")
    Double findAverageScoreByUserIdAndSubject(Long userId, Subject subject);
}