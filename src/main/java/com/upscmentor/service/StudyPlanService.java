package com.upscmentor.service;

import com.upscmentor.model.entity.User;
import com.upscmentor.prompts.SubjectPrompts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StudyPlanService {

    private static final Logger logger = LoggerFactory.getLogger(StudyPlanService.class);

    private final AiModelRouterService aiModelRouterService;
    private final UserService userService;

    public StudyPlanService(AiModelRouterService aiModelRouterService, UserService userService) {
        this.aiModelRouterService = aiModelRouterService;
        this.userService = userService;
    }

    /**
     * Generate a complete personalized study plan
     */
    public String generateStudyPlan(Long userId) {
        try {
            User user = userService.getUserById(userId);

            String prompt = SubjectPrompts.studyPlanPrompt(
                    user.getName(),
                    user.getOptionalSubject().getDisplayName(),
                    user.getDifficultyLevel().getDisplayName(),
                    user.getTargetYear() != null ? user.getTargetYear() : 2025,
                    user.getAttemptNumber() != null ? user.getAttemptNumber() : 1,
                    user.getDailyStudyHours() != null ? user.getDailyStudyHours() : 8,
                    user.getWeakSubjects() != null ? user.getWeakSubjects() : "Not specified",
                    user.getStrongSubjects() != null ? user.getStrongSubjects() : "Not specified"
            );

            logger.info("Generating study plan for user: {}", user.getUsername());

            String plan = aiModelRouterService.generate(user, prompt);

            logger.info("Study plan generated successfully for user: {}", user.getUsername());

            return plan;

        } catch (Exception e) {
            logger.error("Error generating study plan: {}", e.getMessage(), e);
            return "Error generating study plan. Please try again.";
        }
    }

    /**
     * Generate a daily schedule based on available hours
     */
    public String generateDailySchedule(Long userId, String focusSubject) {
        try {
            User user = userService.getUserById(userId);

            String prompt = """
                    Create a detailed daily study schedule for a UPSC aspirant.
                    
                    Student Profile:
                    - Name: %s
                    - Available Hours: %d hours/day
                    - Current Focus Subject: %s
                    - Optional Subject: %s
                    - Preparation Level: %s
                    
                    Create an hour-by-hour schedule including:
                    1. **Morning Block** - Best for new/difficult topics
                    2. **Afternoon Block** - Current affairs & revision
                    3. **Evening Block** - Answer writing practice
                    4. **Night Block** - Light revision & optional subject
                    
                    Include:
                    - Specific time slots (e.g., 6:00 AM - 7:30 AM)
                    - Break times (Pomodoro technique)
                    - Newspaper reading time (The Hindu / Indian Express)
                    - Answer writing practice slot
                    - Revision slot
                    - Optional subject dedicated time
                    - Physical exercise / relaxation time
                    """.formatted(
                    user.getName(),
                    user.getDailyStudyHours() != null ? user.getDailyStudyHours() : 8,
                    focusSubject,
                    user.getOptionalSubject().getDisplayName(),
                    user.getDifficultyLevel().getDisplayName()
            );

            return aiModelRouterService.generate(user, prompt);

        } catch (Exception e) {
            logger.error("Error generating daily schedule: {}", e.getMessage());
            return "Error generating schedule. Please try again.";
        }
    }

    /**
     * Generate weekly revision plan
     */
    public String generateWeeklyRevisionPlan(Long userId) {
        try {
            User user = userService.getUserById(userId);

            String prompt = """
                    Create a weekly revision plan for UPSC preparation.
                    
                    Student: %s
                    Level: %s
                    Weak Subjects: %s
                    Optional: %s
                    
                    Structure:
                    - Monday to Saturday plan (Sunday = light revision + mock test)
                    - Each day: which subjects to revise, specific topics
                    - Include spaced repetition technique
                    - Allocate more time to weak subjects
                    - Include test/quiz schedule
                    - Answer writing practice: minimum 2 answers daily
                    
                    Format as a clean weekly table.
                    """.formatted(
                    user.getName(),
                    user.getDifficultyLevel().getDisplayName(),
                    user.getWeakSubjects() != null ? user.getWeakSubjects() : "Not specified",
                    user.getOptionalSubject().getDisplayName()
            );

            return aiModelRouterService.generate(user, prompt);

        } catch (Exception e) {
            logger.error("Error generating weekly plan: {}", e.getMessage());
            return "Error generating weekly plan. Please try again.";
        }
    }
}
