package com.upscmentor.model.entity;

import com.upscmentor.model.enums.Subject;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress_records",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "record_date", "subject"}))
public class ProgressRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Subject subject;

    @Column(name = "study_hours")
    private Double studyHours;

    @Column(name = "topics_covered")
    private String topicsCovered; // Comma-separated topics studied

    @Column(name = "answers_practiced")
    private Integer answersPracticed;

    @Column(name = "quizzes_completed")
    private Integer quizzesCompleted;

    @Column(name = "quiz_score")
    private Double quizScore; // Average score for the day

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Personal notes for the day

    @Column(name = "confidence_level")
    private Integer confidenceLevel; // 1-5 scale

    @Column(name = "revision_number")
    private Integer revisionNumber; // Which revision (1st, 2nd, 3rd)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (recordDate == null) {
            recordDate = LocalDate.now();
        }
        if (studyHours == null) studyHours = 0.0;
        if (answersPracticed == null) answersPracticed = 0;
        if (quizzesCompleted == null) quizzesCompleted = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public ProgressRecord() {}

    public ProgressRecord(Long userId, Subject subject) {
        this.userId = userId;
        this.subject = subject;
        this.recordDate = LocalDate.now();
    }

    public ProgressRecord(Long userId, Subject subject, LocalDate recordDate) {
        this.userId = userId;
        this.subject = subject;
        this.recordDate = recordDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Double getStudyHours() { return studyHours; }
    public void setStudyHours(Double studyHours) { this.studyHours = studyHours; }

    public String getTopicsCovered() { return topicsCovered; }
    public void setTopicsCovered(String topicsCovered) { this.topicsCovered = topicsCovered; }

    public Integer getAnswersPracticed() { return answersPracticed; }
    public void setAnswersPracticed(Integer answersPracticed) { this.answersPracticed = answersPracticed; }

    public Integer getQuizzesCompleted() { return quizzesCompleted; }
    public void setQuizzesCompleted(Integer quizzesCompleted) { this.quizzesCompleted = quizzesCompleted; }

    public Double getQuizScore() { return quizScore; }
    public void setQuizScore(Double quizScore) { this.quizScore = quizScore; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getConfidenceLevel() { return confidenceLevel; }
    public void setConfidenceLevel(Integer confidenceLevel) { this.confidenceLevel = confidenceLevel; }

    public Integer getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(Integer revisionNumber) { this.revisionNumber = revisionNumber; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Utility Methods
    public void incrementAnswersPracticed() {
        this.answersPracticed = (this.answersPracticed != null ? this.answersPracticed : 0) + 1;
    }

    public void incrementQuizzesCompleted() {
        this.quizzesCompleted = (this.quizzesCompleted != null ? this.quizzesCompleted : 0) + 1;
    }

    public void addStudyHours(double hours) {
        this.studyHours = (this.studyHours != null ? this.studyHours : 0.0) + hours;
    }

    public void addTopicCovered(String topic) {
        if (this.topicsCovered == null || this.topicsCovered.isEmpty()) {
            this.topicsCovered = topic;
        } else {
            this.topicsCovered += ", " + topic;
        }
    }

    @Override
    public String toString() {
        return "ProgressRecord{" +
                "userId=" + userId +
                ", date=" + recordDate +
                ", subject=" + subject +
                ", hours=" + studyHours +
                ", answers=" + answersPracticed +
                ", quizzes=" + quizzesCompleted +
                '}';
    }
}