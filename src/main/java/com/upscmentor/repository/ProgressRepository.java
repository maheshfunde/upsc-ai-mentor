package com.upscmentor.repository;

import com.upscmentor.model.entity.ProgressRecord;
import com.upscmentor.model.enums.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<ProgressRecord, Long> {

    /**
     * Find all progress records for a user, ordered by date
     */
    List<ProgressRecord> findByUserIdOrderByRecordDateDesc(Long userId);

    /**
     * Find progress records for a specific subject
     */
    List<ProgressRecord> findByUserIdAndSubjectOrderByRecordDateDesc(
            Long userId, Subject subject);

    /**
     * Find today's progress record for a user
     */
    Optional<ProgressRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    /**
     * Find today's progress record for a user and specific subject
     */
    Optional<ProgressRecord> findByUserIdAndRecordDateAndSubject(
            Long userId, LocalDate recordDate, Subject subject);

    /**
     * Find progress records within a date range
     */
    List<ProgressRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
            Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get total study hours for a user
     */
    @Query("SELECT COALESCE(SUM(p.studyHours), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId")
    Double getTotalStudyHours(@Param("userId") Long userId);

    /**
     * Get total study hours for a user in a specific subject
     */
    @Query("SELECT COALESCE(SUM(p.studyHours), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId AND p.subject = :subject")
    Double getTotalStudyHoursBySubject(
            @Param("userId") Long userId,
            @Param("subject") Subject subject);

    /**
     * Get average daily study hours for records since a given date
     */
    @Query("SELECT COALESCE(AVG(p.studyHours), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId AND p.recordDate >= :startDate")
    Double getAverageDailyStudyHours(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    /**
     * Count total study days for a user
     */
    @Query("SELECT COUNT(DISTINCT p.recordDate) FROM ProgressRecord p " +
            "WHERE p.userId = :userId")
    Long countStudyDays(@Param("userId") Long userId);

    /**
     * Get study hours grouped by subject (JPQL compatible)
     */
    @Query("SELECT p.subject, SUM(p.studyHours) FROM ProgressRecord p " +
            "WHERE p.userId = :userId GROUP BY p.subject " +
            "ORDER BY SUM(p.studyHours) DESC")
    List<Object[]> getStudyHoursBySubject(@Param("userId") Long userId);

    /**
     * Get daily study hours for a date range (for charts)
     */
    @Query("SELECT p.recordDate, SUM(p.studyHours) FROM ProgressRecord p " +
            "WHERE p.userId = :userId AND p.recordDate >= :startDate " +
            "GROUP BY p.recordDate ORDER BY p.recordDate ASC")
    List<Object[]> getDailyStudyHoursForPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    /**
     * Get recent study dates (for streak calculation in Java)
     * Returns dates in descending order
     */
    @Query("SELECT DISTINCT p.recordDate FROM ProgressRecord p " +
            "WHERE p.userId = :userId " +
            "ORDER BY p.recordDate DESC")
    List<LocalDate> getStudyDatesDescending(@Param("userId") Long userId);

    /**
     * Get total answers practiced
     */
    @Query("SELECT COALESCE(SUM(p.answersPracticed), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId")
    Long getTotalAnswersPracticed(@Param("userId") Long userId);

    /**
     * Get total quizzes completed
     */
    @Query("SELECT COALESCE(SUM(p.quizzesCompleted), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId")
    Long getTotalQuizzesCompleted(@Param("userId") Long userId);

    /**
     * Get total answers practiced in a date range
     */
    @Query("SELECT COALESCE(SUM(p.answersPracticed), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId " +
            "AND p.recordDate BETWEEN :startDate AND :endDate")
    Long getAnswersPracticedInRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get average quiz score for a period
     */
    @Query("SELECT COALESCE(AVG(p.quizScore), 0) FROM ProgressRecord p " +
            "WHERE p.userId = :userId AND p.quizScore IS NOT NULL " +
            "AND p.recordDate >= :startDate")
    Double getAverageQuizScoreSince(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate);

    /**
     * Get confidence level trend (average by month)
     */
    @Query("SELECT FUNCTION('MONTH', p.recordDate), AVG(p.confidenceLevel) " +
            "FROM ProgressRecord p " +
            "WHERE p.userId = :userId AND p.confidenceLevel IS NOT NULL " +
            "AND FUNCTION('YEAR', p.recordDate) = :year " +
            "GROUP BY FUNCTION('MONTH', p.recordDate) " +
            "ORDER BY FUNCTION('MONTH', p.recordDate)")
    List<Object[]> getMonthlyConfidenceTrend(
            @Param("userId") Long userId,
            @Param("year") int year);

    /**
     * Get study hours trend by month
     */
    @Query("SELECT FUNCTION('MONTH', p.recordDate), SUM(p.studyHours) " +
            "FROM ProgressRecord p " +
            "WHERE p.userId = :userId " +
            "AND FUNCTION('YEAR', p.recordDate) = :year " +
            "GROUP BY FUNCTION('MONTH', p.recordDate) " +
            "ORDER BY FUNCTION('MONTH', p.recordDate)")
    List<Object[]> getMonthlyStudyHours(
            @Param("userId") Long userId,
            @Param("year") int year);

    /**
     * Get subjects studied on a specific date
     */
    List<ProgressRecord> findByUserIdAndRecordDateOrderBySubject(
            Long userId, LocalDate recordDate);

    /**
     * Delete old progress records (data cleanup)
     */
    @Modifying
    @Transactional
    void deleteByUserIdAndRecordDateBefore(Long userId, LocalDate date);

    /**
     * Check if user has a record for today
     */
    boolean existsByUserIdAndRecordDate(Long userId, LocalDate recordDate);
}