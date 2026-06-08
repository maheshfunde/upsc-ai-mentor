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

                    UPSC Daily Schedule Requirements:
                    1. **Morning Block (3 hours)** — New/difficult topics (fresh mind for complex subjects)
                    2. **Newspaper + Current Affairs (1.5 hours)** — The Hindu/Indian Express + monthly compilation
                    3. **Afternoon Block (2 hours)** — Standard book reading + note-making for current focus subject
                    4. **Answer Writing Practice (1 hour)** — 2 Mains answers (150 or 250 words each)
                    5. **Evening Block (1 hour)** — Revision of previous day's topics
                    6. **Night Block (1 hour)** — Optional subject reading
                    7. **Non-Negotiables:** 30 min exercise, 7 hours sleep, 1 hour recreation

                    Format as a time-table with:
                    - Specific time slots (e.g., 6:00 AM - 8:00 AM)
                    - Pomodoro breaks (5 min after every 50 min study)
                    - Buffer time (30 min) for overflow
                    - Current affairs integration: link today's news to static syllabus topics being studied
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
                    Create a weekly revision plan for UPSC preparation using spaced repetition.

                    Student: %s
                    Level: %s
                    Weak Subjects: %s
                    Optional: %s

                    UPSC Weekly Revision Framework:
                    - **Spaced Repetition Cycle:** 1-day → 3-day → 7-day → 30-day revision intervals
                    - **Monday-Thursday:** Cover new topics + revise previous week's content (1-day/3-day cycle)
                    - **Friday:** Weekly consolidated revision (7-day cycle)
                    - **Saturday:** PYQ practice + targeted weak subject revision
                    - **Sunday:** Full-length mock test + analysis + 30-day cycle revision

                    For each day specify:
                    - Subjects to revise with specific topics/chapters
                    - Time allocation (prioritize weak subjects — give 40%% extra time)
                    - Revision technique (active recall, mind maps, flashcards, or written answers)
                    - Minimum 2 Mains answer writing practice daily
                    - 1 quiz or mini-test per subject weekly

                    Format as a clean weekly table with subject, topic, time, and revision technique columns.
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
