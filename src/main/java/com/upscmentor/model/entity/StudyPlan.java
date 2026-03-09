package com.upscmentor.model.entity;

import com.upscmentor.model.enums.DifficultyLevel;
import com.upscmentor.model.enums.Subject;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ===== Plan Metadata =====

    @Column(name = "plan_title", nullable = false)
    private String planTitle;

    @Column(name = "plan_type", nullable = false)
    private String planType; // FULL, DAILY, WEEKLY, MONTHLY, REVISION, CUSTOM

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "target_exam_year")
    private Integer targetExamYear;

    // ===== Date Range =====

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    // ===== Plan Content =====

    @Column(name = "plan_content", columnDefinition = "CLOB")
    private String planContent; // AI-generated markdown content

    @Column(name = "daily_schedule", columnDefinition = "CLOB")
    private String dailySchedule; // JSON string of daily schedule

    @Column(name = "weekly_schedule", columnDefinition = "CLOB")
    private String weeklySchedule; // JSON string of weekly schedule

    // ===== Subject Focus =====

    @Enumerated(EnumType.STRING)
    @Column(name = "focus_subject")
    private Subject focusSubject;

    @Column(name = "optional_subject_name")
    private String optionalSubjectName;

    @Column(name = "weak_subjects")
    private String weakSubjects; // Comma-separated

    @Column(name = "strong_subjects")
    private String strongSubjects; // Comma-separated

    // ===== Study Parameters =====

    @Column(name = "daily_study_hours")
    private Integer dailyStudyHours;

    @Column(name = "answer_writing_per_day")
    private Integer answerWritingPerDay;

    @Column(name = "revision_cycles")
    private Integer revisionCycles;

    @Column(name = "mock_test_frequency")
    private String mockTestFrequency; // "WEEKLY", "BIWEEKLY", "MONTHLY"

    // ===== Phase Tracking =====

    @Column(name = "current_phase")
    private String currentPhase; // "FOUNDATION", "ADVANCED", "REVISION", "TEST_SERIES"

    @Column(name = "current_phase_number")
    private Integer currentPhaseNumber;

    @Column(name = "total_phases")
    private Integer totalPhases;

    // ===== Progress =====

    @Column(name = "completion_percentage")
    private Double completionPercentage;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_archived")
    private Boolean isArchived;

    // ===== Books & Resources =====

    @Column(name = "recommended_books", columnDefinition = "TEXT")
    private String recommendedBooks; // JSON array as string

    @Column(name = "recommended_resources", columnDefinition = "TEXT")
    private String recommendedResources; // JSON array as string

    // ===== Milestones =====

    @Column(name = "milestones", columnDefinition = "TEXT")
    private String milestones; // JSON array of milestones with dates and status

    @Column(name = "next_milestone")
    private String nextMilestone;

    @Column(name = "next_milestone_date")
    private LocalDate nextMilestoneDate;

    // ===== AI Generation Metadata =====

    @Column(name = "ai_model_used")
    private String aiModelUsed;

    @Column(name = "prompt_used", columnDefinition = "TEXT")
    private String promptUsed; // Store the prompt for regeneration

    @Column(name = "generation_parameters", columnDefinition = "TEXT")
    private String generationParameters; // JSON of parameters used

    // ===== Notes =====

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes; // User's personal notes on this plan

    @Column(name = "ai_suggestions", columnDefinition = "TEXT")
    private String aiSuggestions; // AI-generated suggestions for improvement

    // ===== Timestamps =====

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    // ===== Lifecycle Callbacks =====

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) isActive = true;
        if (isArchived == null) isArchived = false;
        if (completionPercentage == null) completionPercentage = 0.0;
        if (currentPhaseNumber == null) currentPhaseNumber = 1;
        if (answerWritingPerDay == null) answerWritingPerDay = 2;
        if (revisionCycles == null) revisionCycles = 3;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Constructors =====

    public StudyPlan() {}

    /**
     * Constructor for quick plan creation
     */
    public StudyPlan(Long userId, String planTitle, String planType) {
        this.userId = userId;
        this.planTitle = planTitle;
        this.planType = planType;
    }

    /**
     * Full constructor for comprehensive plan
     */
    public StudyPlan(Long userId, String planTitle, String planType,
                     DifficultyLevel difficultyLevel, Integer targetExamYear,
                     LocalDate startDate, LocalDate endDate,
                     Integer dailyStudyHours, String optionalSubjectName) {
        this.userId = userId;
        this.planTitle = planTitle;
        this.planType = planType;
        this.difficultyLevel = difficultyLevel;
        this.targetExamYear = targetExamYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dailyStudyHours = dailyStudyHours;
        this.optionalSubjectName = optionalSubjectName;

        // Calculate duration
        if (startDate != null && endDate != null) {
            this.durationWeeks = (int) ((endDate.toEpochDay() - startDate.toEpochDay()) / 7);
        }
    }

    // ===== Getters and Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPlanTitle() { return planTitle; }
    public void setPlanTitle(String planTitle) { this.planTitle = planTitle; }

    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public Integer getTargetExamYear() { return targetExamYear; }
    public void setTargetExamYear(Integer targetExamYear) { this.targetExamYear = targetExamYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(Integer durationWeeks) { this.durationWeeks = durationWeeks; }

    public String getPlanContent() { return planContent; }
    public void setPlanContent(String planContent) { this.planContent = planContent; }

    public String getDailySchedule() { return dailySchedule; }
    public void setDailySchedule(String dailySchedule) { this.dailySchedule = dailySchedule; }

    public String getWeeklySchedule() { return weeklySchedule; }
    public void setWeeklySchedule(String weeklySchedule) { this.weeklySchedule = weeklySchedule; }

    public Subject getFocusSubject() { return focusSubject; }
    public void setFocusSubject(Subject focusSubject) { this.focusSubject = focusSubject; }

    public String getOptionalSubjectName() { return optionalSubjectName; }
    public void setOptionalSubjectName(String optionalSubjectName) { this.optionalSubjectName = optionalSubjectName; }

    public String getWeakSubjects() { return weakSubjects; }
    public void setWeakSubjects(String weakSubjects) { this.weakSubjects = weakSubjects; }

    public String getStrongSubjects() { return strongSubjects; }
    public void setStrongSubjects(String strongSubjects) { this.strongSubjects = strongSubjects; }

    public Integer getDailyStudyHours() { return dailyStudyHours; }
    public void setDailyStudyHours(Integer dailyStudyHours) { this.dailyStudyHours = dailyStudyHours; }

    public Integer getAnswerWritingPerDay() { return answerWritingPerDay; }
    public void setAnswerWritingPerDay(Integer answerWritingPerDay) { this.answerWritingPerDay = answerWritingPerDay; }

    public Integer getRevisionCycles() { return revisionCycles; }
    public void setRevisionCycles(Integer revisionCycles) { this.revisionCycles = revisionCycles; }

    public String getMockTestFrequency() { return mockTestFrequency; }
    public void setMockTestFrequency(String mockTestFrequency) { this.mockTestFrequency = mockTestFrequency; }

    public String getCurrentPhase() { return currentPhase; }
    public void setCurrentPhase(String currentPhase) { this.currentPhase = currentPhase; }

    public Integer getCurrentPhaseNumber() { return currentPhaseNumber; }
    public void setCurrentPhaseNumber(Integer currentPhaseNumber) { this.currentPhaseNumber = currentPhaseNumber; }

    public Integer getTotalPhases() { return totalPhases; }
    public void setTotalPhases(Integer totalPhases) { this.totalPhases = totalPhases; }

    public Double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Double completionPercentage) { this.completionPercentage = completionPercentage; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsArchived() { return isArchived; }
    public void setIsArchived(Boolean isArchived) { this.isArchived = isArchived; }

    public String getRecommendedBooks() { return recommendedBooks; }
    public void setRecommendedBooks(String recommendedBooks) { this.recommendedBooks = recommendedBooks; }

    public String getRecommendedResources() { return recommendedResources; }
    public void setRecommendedResources(String recommendedResources) { this.recommendedResources = recommendedResources; }

    public String getMilestones() { return milestones; }
    public void setMilestones(String milestones) { this.milestones = milestones; }

    public String getNextMilestone() { return nextMilestone; }
    public void setNextMilestone(String nextMilestone) { this.nextMilestone = nextMilestone; }

    public LocalDate getNextMilestoneDate() { return nextMilestoneDate; }
    public void setNextMilestoneDate(LocalDate nextMilestoneDate) { this.nextMilestoneDate = nextMilestoneDate; }

    public String getAiModelUsed() { return aiModelUsed; }
    public void setAiModelUsed(String aiModelUsed) { this.aiModelUsed = aiModelUsed; }

    public String getPromptUsed() { return promptUsed; }
    public void setPromptUsed(String promptUsed) { this.promptUsed = promptUsed; }

    public String getGenerationParameters() { return generationParameters; }
    public void setGenerationParameters(String generationParameters) { this.generationParameters = generationParameters; }

    public String getUserNotes() { return userNotes; }
    public void setUserNotes(String userNotes) { this.userNotes = userNotes; }

    public String getAiSuggestions() { return aiSuggestions; }
    public void setAiSuggestions(String aiSuggestions) { this.aiSuggestions = aiSuggestions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public LocalDateTime getLastReviewedAt() { return lastReviewedAt; }
    public void setLastReviewedAt(LocalDateTime lastReviewedAt) { this.lastReviewedAt = lastReviewedAt; }

    // ===== Utility Methods =====

    /**
     * Check if the plan is still within its date range
     */
    public boolean isWithinDateRange() {
        if (startDate == null || endDate == null) return true;
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Calculate days remaining until end date
     */
    public long getDaysRemaining() {
        if (endDate == null) return -1;
        long days = endDate.toEpochDay() - LocalDate.now().toEpochDay();
        return Math.max(0, days);
    }

    /**
     * Calculate days since plan started
     */
    public long getDaysSinceStart() {
        if (startDate == null) return 0;
        long days = LocalDate.now().toEpochDay() - startDate.toEpochDay();
        return Math.max(0, days);
    }

    /**
     * Calculate time-based progress percentage
     * (What percentage of total time has elapsed)
     */
    public double getTimeProgressPercentage() {
        if (startDate == null || endDate == null) return 0.0;
        long totalDays = endDate.toEpochDay() - startDate.toEpochDay();
        if (totalDays <= 0) return 100.0;
        long elapsed = getDaysSinceStart();
        return Math.min(100.0, (double) elapsed / totalDays * 100);
    }

    /**
     * Check if the plan needs attention
     * (time progress is ahead of completion progress)
     */
    public boolean needsAttention() {
        double timeProgress = getTimeProgressPercentage();
        double completion = completionPercentage != null ? completionPercentage : 0.0;
        return timeProgress - completion > 15.0; // Behind by more than 15%
    }

    /**
     * Get a status label based on plan state
     */
    public String getStatusLabel() {
        if (isArchived != null && isArchived) return "📦 Archived";
        if (isActive == null || !isActive) return "⏸️ Paused";
        if (needsAttention()) return "⚠️ Needs Attention";
        if (completionPercentage != null && completionPercentage >= 100) return "✅ Completed";
        if (isWithinDateRange()) return "🟢 Active";
        if (getDaysRemaining() == 0) return "🔴 Overdue";
        return "📋 Planned";
    }

    /**
     * Get the current phase display name
     */
    public String getCurrentPhaseDisplay() {
        if (currentPhase == null) return "Not Started";
        return switch (currentPhase.toUpperCase()) {
            case "FOUNDATION" -> "📚 Foundation Phase";
            case "ADVANCED" -> "🎯 Advanced Phase";
            case "REVISION" -> "🔁 Revision Phase";
            case "TEST_SERIES" -> "📝 Test Series Phase";
            case "FINAL_REVISION" -> "⚡ Final Revision";
            case "INTERVIEW_PREP" -> "🎤 Interview Preparation";
            default -> "📋 " + currentPhase;
        };
    }

    /**
     * Advance to next phase
     */
    public void advancePhase() {
        if (currentPhaseNumber != null && totalPhases != null
                && currentPhaseNumber < totalPhases) {
            currentPhaseNumber++;

            // Auto-set phase name based on number
            currentPhase = switch (currentPhaseNumber) {
                case 1 -> "FOUNDATION";
                case 2 -> "ADVANCED";
                case 3 -> "REVISION";
                case 4 -> "TEST_SERIES";
                case 5 -> "FINAL_REVISION";
                default -> "PHASE_" + currentPhaseNumber;
            };
        }
    }

    /**
     * Update completion percentage
     */
    public void updateCompletion(double percentage) {
        this.completionPercentage = Math.min(100.0, Math.max(0.0, percentage));
        if (this.completionPercentage >= 100.0) {
            this.isActive = false;
        }
    }

    /**
     * Archive the plan
     */
    public void archive() {
        this.isArchived = true;
        this.isActive = false;
    }

    /**
     * Reactivate an archived plan
     */
    public void reactivate() {
        this.isArchived = false;
        this.isActive = true;
    }

    @Override
    public String toString() {
        return "StudyPlan{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + planTitle + '\'' +
                ", type='" + planType + '\'' +
                ", phase='" + currentPhase + '\'' +
                ", completion=" + completionPercentage + "%" +
                ", status=" + getStatusLabel() +
                ", daysRemaining=" + getDaysRemaining() +
                '}';
    }
}