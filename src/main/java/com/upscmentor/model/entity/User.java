package com.upscmentor.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.upscmentor.model.enums.DifficultyLevel;
import com.upscmentor.model.enums.OptionalSubject;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "optional_subject")
    private OptionalSubject optionalSubject;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;

    @Column(name = "target_year")
    private Integer targetYear;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "daily_study_hours")
    private Integer dailyStudyHours;

    @Column(name = "weak_subjects")
    private String weakSubjects; // Comma-separated

    @Column(name = "strong_subjects")
    private String strongSubjects; // Comma-separated

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "openai_api_key", length = 300)
    private String openAiApiKey;

    @Column(name = "online_model_name")
    private String onlineModelName;

    @Column(name = "online_base_url")
    private String onlineBaseUrl;

    @Column(name = "local_model_name")
    private String localModelName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }

    // Default constructor
    public User() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public OptionalSubject getOptionalSubject() { return optionalSubject; }
    public void setOptionalSubject(OptionalSubject optionalSubject) { this.optionalSubject = optionalSubject; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public Integer getTargetYear() { return targetYear; }
    public void setTargetYear(Integer targetYear) { this.targetYear = targetYear; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public Integer getDailyStudyHours() { return dailyStudyHours; }
    public void setDailyStudyHours(Integer dailyStudyHours) { this.dailyStudyHours = dailyStudyHours; }

    public String getWeakSubjects() { return weakSubjects; }
    public void setWeakSubjects(String weakSubjects) { this.weakSubjects = weakSubjects; }

    public String getStrongSubjects() { return strongSubjects; }
    public void setStrongSubjects(String strongSubjects) { this.strongSubjects = strongSubjects; }

    public String getOpenAiApiKey() { return openAiApiKey; }
    public void setOpenAiApiKey(String openAiApiKey) { this.openAiApiKey = openAiApiKey; }

    public String getOnlineModelName() { return onlineModelName; }
    public void setOnlineModelName(String onlineModelName) { this.onlineModelName = onlineModelName; }

    public String getOnlineBaseUrl() { return onlineBaseUrl; }
    public void setOnlineBaseUrl(String onlineBaseUrl) { this.onlineBaseUrl = onlineBaseUrl; }

    public String getLocalModelName() { return localModelName; }
    public void setLocalModelName(String localModelName) { this.localModelName = localModelName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
}
