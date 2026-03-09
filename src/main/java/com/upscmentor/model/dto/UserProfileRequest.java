package com.upscmentor.model.dto;

import com.upscmentor.model.enums.DifficultyLevel;
import com.upscmentor.model.enums.OptionalSubject;
import jakarta.validation.constraints.*;

public class UserProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    private String username;

    @NotNull(message = "Please select an optional subject")
    private OptionalSubject optionalSubject;

    @NotNull(message = "Please select your preparation level")
    private DifficultyLevel difficultyLevel;

    @Min(value = 2024) @Max(value = 2030)
    private Integer targetYear;

    @Min(value = 1) @Max(value = 10)
    private Integer attemptNumber;

    @Min(value = 1) @Max(value = 16)
    private Integer dailyStudyHours;

    private String weakSubjects;
    private String strongSubjects;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
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
}