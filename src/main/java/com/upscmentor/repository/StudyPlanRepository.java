package com.upscmentor.repository;

import com.upscmentor.model.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    /**
     * Find all plans for a user (ordered by latest first)
     */
    List<StudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find active plans for a user
     */
    List<StudyPlan> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);

    /**
     * Find archived plans for a user
     */
    List<StudyPlan> findByUserIdAndIsArchivedTrueOrderByCreatedAtDesc(Long userId);

    /**
     * Find the latest active plan of a specific type
     */
    Optional<StudyPlan> findFirstByUserIdAndPlanTypeAndIsActiveTrueOrderByCreatedAtDesc(
            Long userId, String planType);

    /**
     * Find plans by type
     */
    List<StudyPlan> findByUserIdAndPlanTypeOrderByCreatedAtDesc(Long userId, String planType);

    /**
     * Count active plans for a user
     */
    Long countByUserIdAndIsActiveTrue(Long userId);

    /**
     * Get the most recently created plan
     */
    Optional<StudyPlan> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find active plans that are overdue (end date has passed)
     * Works with H2, PostgreSQL, MySQL
     */
    @Query("SELECT sp FROM StudyPlan sp WHERE sp.userId = :userId " +
            "AND sp.isActive = true " +
            "AND sp.endDate IS NOT NULL " +
            "AND sp.endDate < CURRENT_DATE")
    List<StudyPlan> findOverduePlans(@Param("userId") Long userId);

    /**
     * Find active plans where completion is less than expected
     * Uses JPQL compatible syntax (no DATEDIFF)
     */
    @Query("SELECT sp FROM StudyPlan sp WHERE sp.userId = :userId " +
            "AND sp.isActive = true " +
            "AND sp.completionPercentage < 50.0 " +
            "AND sp.endDate IS NOT NULL " +
            "AND sp.endDate < :warningDate")
    List<StudyPlan> findPlansNeedingAttention(
            @Param("userId") Long userId,
            @Param("warningDate") LocalDate warningDate);

    /**
     * Find plans expiring within a date range
     */
    @Query("SELECT sp FROM StudyPlan sp WHERE sp.userId = :userId " +
            "AND sp.isActive = true " +
            "AND sp.endDate IS NOT NULL " +
            "AND sp.endDate BETWEEN CURRENT_DATE AND :endDate")
    List<StudyPlan> findPlansExpiringSoon(
            @Param("userId") Long userId,
            @Param("endDate") LocalDate endDate);

    /**
     * Find plans by current phase
     */
    List<StudyPlan> findByUserIdAndCurrentPhaseAndIsActiveTrue(
            Long userId, String currentPhase);

    /**
     * Deactivate all plans of a type for a user (when creating new one)
     */
    @Modifying
    @Transactional
    @Query("UPDATE StudyPlan sp SET sp.isActive = false " +
            "WHERE sp.userId = :userId AND sp.planType = :planType AND sp.isActive = true")
    void deactivatePlansByType(
            @Param("userId") Long userId,
            @Param("planType") String planType);

    /**
     * Update completion percentage for a plan
     */
    @Modifying
    @Transactional
    @Query("UPDATE StudyPlan sp SET sp.completionPercentage = :percentage, " +
            "sp.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE sp.id = :planId AND sp.userId = :userId")
    void updateCompletion(
            @Param("planId") Long planId,
            @Param("userId") Long userId,
            @Param("percentage") Double percentage);

    /**
     * Update current phase for a plan
     */
    @Modifying
    @Transactional
    @Query("UPDATE StudyPlan sp SET sp.currentPhase = :phase, " +
            "sp.currentPhaseNumber = :phaseNumber, " +
            "sp.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE sp.id = :planId AND sp.userId = :userId")
    void updatePhase(
            @Param("planId") Long planId,
            @Param("userId") Long userId,
            @Param("phase") String phase,
            @Param("phaseNumber") Integer phaseNumber);

    /**
     * Archive a plan
     */
    @Modifying
    @Transactional
    @Query("UPDATE StudyPlan sp SET sp.isArchived = true, sp.isActive = false, " +
            "sp.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE sp.id = :planId AND sp.userId = :userId")
    void archivePlan(@Param("planId") Long planId, @Param("userId") Long userId);

    /**
     * Reactivate an archived plan
     */
    @Modifying
    @Transactional
    @Query("UPDATE StudyPlan sp SET sp.isArchived = false, sp.isActive = true, " +
            "sp.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE sp.id = :planId AND sp.userId = :userId")
    void reactivatePlan(@Param("planId") Long planId, @Param("userId") Long userId);

    /**
     * Get average completion across all active plans
     */
    @Query("SELECT COALESCE(AVG(sp.completionPercentage), 0.0) FROM StudyPlan sp " +
            "WHERE sp.userId = :userId AND sp.isActive = true")
    Double getAverageCompletion(@Param("userId") Long userId);

    /**
     * Count completed plans
     */
    @Query("SELECT COUNT(sp) FROM StudyPlan sp WHERE sp.userId = :userId " +
            "AND sp.completionPercentage >= 100.0")
    Long countCompletedPlans(@Param("userId") Long userId);

    /**
     * Get plans created in a date range
     */
    List<StudyPlan> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Delete old archived plans (cleanup)
     */
    @Modifying
    @Transactional
    void deleteByUserIdAndIsArchivedTrueAndCreatedAtBefore(
            Long userId, LocalDateTime before);

    /**
     * Check if user has any active plan
     */
    boolean existsByUserIdAndIsActiveTrue(Long userId);
}