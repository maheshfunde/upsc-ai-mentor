package com.upscmentor.service;

import com.upscmentor.model.entity.QuizResult;
import com.upscmentor.model.entity.User;
import com.upscmentor.model.entity.ProgressRecord;
import com.upscmentor.model.entity.StudyPlan;
import com.upscmentor.model.enums.Subject;
import com.upscmentor.repository.ChatHistoryRepository;
import com.upscmentor.repository.QuizResultRepository;
import com.upscmentor.repository.ProgressRepository;
import com.upscmentor.repository.StudyPlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private static final Logger logger = LoggerFactory.getLogger(ProgressService.class);

    private final QuizResultRepository quizResultRepository;
    private final ChatHistoryRepository chatHistoryRepository;
    private final ProgressRepository progressRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final UserService userService;
    private final AiModelRouterService aiModelRouterService;

    public ProgressService(QuizResultRepository quizResultRepository,
                           ChatHistoryRepository chatHistoryRepository,
                           ProgressRepository progressRepository,
                           StudyPlanRepository studyPlanRepository,
                           UserService userService,
                           AiModelRouterService aiModelRouterService) {
        this.quizResultRepository = quizResultRepository;
        this.chatHistoryRepository = chatHistoryRepository;
        this.progressRepository = progressRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.userService = userService;
        this.aiModelRouterService = aiModelRouterService;
    }

    // ==========================================================
    //  MAIN DASHBOARD DATA METHOD
    // ==========================================================

    /**
     * Get comprehensive progress dashboard data
     * This is the main method that aggregates ALL data for the dashboard
     */
    public Map<String, Object> getDashboardData(Long userId) {
        User user = userService.getUserById(userId);
        Map<String, Object> dashboard = new LinkedHashMap<>();

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 1. USER PROFILE INFORMATION
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        dashboard.put("userName", user.getName());
        dashboard.put("username", user.getUsername());
        dashboard.put("optionalSubject",
                user.getOptionalSubject() != null
                        ? user.getOptionalSubject().getDisplayName()
                        : "Not Selected");
        dashboard.put("targetYear",
                user.getTargetYear() != null ? user.getTargetYear() : 2025);
        dashboard.put("level",
                user.getDifficultyLevel() != null
                        ? user.getDifficultyLevel().getDisplayName()
                        : "Not Set");
        dashboard.put("attemptNumber",
                user.getAttemptNumber() != null ? user.getAttemptNumber() : 1);
        dashboard.put("dailyStudyHoursTarget",
                user.getDailyStudyHours() != null ? user.getDailyStudyHours() : 8);
        dashboard.put("weakSubjects",
                user.getWeakSubjects() != null ? user.getWeakSubjects() : "Not specified");
        dashboard.put("strongSubjects",
                user.getStrongSubjects() != null ? user.getStrongSubjects() : "Not specified");
        dashboard.put("lastActive", user.getLastActive());

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 2. QUIZ STATISTICS
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> quizStats = getQuizStatistics(userId);
        dashboard.put("quizStats", quizStats);
        // Also put top-level for backward compatibility
        dashboard.put("totalQuizzes", quizStats.get("totalQuizzes"));
        dashboard.put("averageScore", quizStats.get("averageScore"));

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 3. SUBJECT-WISE SCORES
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Double> subjectScores = getSubjectWiseScores(userId);
        dashboard.put("subjectScores", subjectScores);

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 4. CHAT SESSION STATISTICS
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> chatStats = getChatStatistics(userId);
        dashboard.put("chatStats", chatStats);
        dashboard.put("totalChatSessions", chatStats.get("totalSessions"));

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 5. STUDY PROGRESS (from ProgressRecord)
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> studyProgress = getStudyProgress(userId);
        dashboard.put("studyProgress", studyProgress);

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 6. STREAK DATA
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> streakData = getStreakData(userId);
        dashboard.put("streakData", streakData);
        dashboard.put("currentStreak", streakData.get("currentStreak"));
        dashboard.put("longestStreak", streakData.get("longestStreak"));

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 7. STUDY PLAN STATUS
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> planStatus = getStudyPlanStatus(userId);
        dashboard.put("studyPlanStatus", planStatus);

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 8. WEAK & STRONG SUBJECTS (from quiz data)
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> subjectAnalysis = analyzeSubjectStrengths(subjectScores);
        dashboard.put("weakSubjectsFromQuiz", subjectAnalysis.get("weakSubjects"));
        dashboard.put("strongSubjectsFromQuiz", subjectAnalysis.get("strongSubjects"));
        dashboard.put("averageSubjects", subjectAnalysis.get("averageSubjects"));

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 9. RECENT ACTIVITY
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> recentActivity = getRecentActivity(userId);
        dashboard.put("recentActivity", recentActivity);

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 10. EXAM COUNTDOWN
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> examCountdown = getExamCountdown(user.getTargetYear());
        dashboard.put("examCountdown", examCountdown);

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 11. TODAY'S SUMMARY
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> todaySummary = getTodaySummary(userId);
        dashboard.put("todaySummary", todaySummary);

        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // 12. WEEKLY SUMMARY
        // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Map<String, Object> weeklySummary = getWeeklySummary(userId);
        dashboard.put("weeklySummary", weeklySummary);

        logger.info("Dashboard data loaded for user: {} ({})",
                user.getUsername(), user.getName());

        return dashboard;
    }

    // ==========================================================
    //  QUIZ STATISTICS
    // ==========================================================

    private Map<String, Object> getQuizStatistics(Long userId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        List<QuizResult> quizResults = quizResultRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        Double avgScore = quizResultRepository.findAverageScoreByUserId(userId);

        int totalQuizzes = quizResults.size();
        int totalQuestions = quizResults.stream()
                .filter(q -> q.getTotalQuestions() != null)
                .mapToInt(QuizResult::getTotalQuestions)
                .sum();
        int totalCorrect = quizResults.stream()
                .filter(q -> q.getCorrectAnswers() != null)
                .mapToInt(QuizResult::getCorrectAnswers)
                .sum();

        stats.put("totalQuizzes", totalQuizzes);
        stats.put("totalQuestionsAttempted", totalQuestions);
        stats.put("totalCorrectAnswers", totalCorrect);
        stats.put("averageScore", avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);
        stats.put("accuracy",
                totalQuestions > 0
                        ? Math.round((double) totalCorrect / totalQuestions * 100 * 10.0) / 10.0
                        : 0.0);

        // Recent 5 quizzes
        List<Map<String, Object>> recentQuizzes = quizResults.stream()
                .limit(5)
                .map(q -> {
                    Map<String, Object> quiz = new LinkedHashMap<>();
                    quiz.put("subject", q.getSubject() != null ? q.getSubject().getDisplayName() : "General");
                    quiz.put("score", q.getScorePercentage());
                    quiz.put("correct", q.getCorrectAnswers());
                    quiz.put("total", q.getTotalQuestions());
                    quiz.put("date", q.getCreatedAt());
                    return quiz;
                })
                .collect(Collectors.toList());
        stats.put("recentQuizzes", recentQuizzes);

        // Best and worst quiz scores
        OptionalDouble bestScore = quizResults.stream()
                .filter(q -> q.getScorePercentage() != null)
                .mapToDouble(QuizResult::getScorePercentage)
                .max();
        OptionalDouble worstScore = quizResults.stream()
                .filter(q -> q.getScorePercentage() != null)
                .mapToDouble(QuizResult::getScorePercentage)
                .min();

        stats.put("bestScore", bestScore.isPresent() ? bestScore.getAsDouble() : 0.0);
        stats.put("worstScore", worstScore.isPresent() ? worstScore.getAsDouble() : 0.0);

        // Quiz trend (last 10 quizzes - are scores improving?)
        List<Double> recentScores = quizResults.stream()
                .limit(10)
                .filter(q -> q.getScorePercentage() != null)
                .map(QuizResult::getScorePercentage)
                .collect(Collectors.toList());
        Collections.reverse(recentScores); // Oldest first for trend
        stats.put("scoreTrend", recentScores);
        stats.put("trendDirection", calculateTrend(recentScores));

        return stats;
    }

    // ==========================================================
    //  SUBJECT-WISE SCORES
    // ==========================================================

    private Map<String, Double> getSubjectWiseScores(Long userId) {
        Map<String, Double> subjectScores = new LinkedHashMap<>();

        for (Subject subject : Subject.values()) {
            Double avg = quizResultRepository
                    .findAverageScoreByUserIdAndSubject(userId, subject);
            if (avg != null) {
                subjectScores.put(
                        subject.getDisplayName(),
                        Math.round(avg * 10.0) / 10.0
                );
            }
        }

        return subjectScores;
    }

    // ==========================================================
    //  CHAT STATISTICS
    // ==========================================================

    private Map<String, Object> getChatStatistics(Long userId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        var chatHistory = chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // Count unique sessions
        long totalSessions = chatHistory.stream()
                .map(ch -> ch.getSessionId())
                .distinct()
                .count();

        // Count total messages
        long totalMessages = chatHistory.size();
        long userMessages = chatHistory.stream()
                .filter(ch -> "USER".equals(ch.getRole()))
                .count();
        long aiMessages = chatHistory.stream()
                .filter(ch -> "ASSISTANT".equals(ch.getRole()))
                .count();

        // Subjects discussed
        Map<String, Long> subjectCounts = chatHistory.stream()
                .filter(ch -> ch.getSubject() != null)
                .collect(Collectors.groupingBy(
                        ch -> ch.getSubject().getDisplayName(),
                        Collectors.counting()
                ));

        // Most discussed subject
        String mostDiscussedSubject = subjectCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None yet");

        stats.put("totalSessions", totalSessions);
        stats.put("totalMessages", totalMessages);
        stats.put("userMessages", userMessages);
        stats.put("aiMessages", aiMessages);
        stats.put("subjectCounts", subjectCounts);
        stats.put("mostDiscussedSubject", mostDiscussedSubject);

        return stats;
    }

    // ==========================================================
    //  STUDY PROGRESS (from ProgressRecord)
    // ==========================================================

    private Map<String, Object> getStudyProgress(Long userId) {
        Map<String, Object> progress = new LinkedHashMap<>();

        // Total study hours
        Double totalHours = progressRepository.getTotalStudyHours(userId);
        progress.put("totalStudyHours",
                totalHours != null ? Math.round(totalHours * 10.0) / 10.0 : 0.0);

        // Total study days
        Long studyDays = progressRepository.countStudyDays(userId);
        progress.put("totalStudyDays", studyDays != null ? studyDays : 0L);

        // Average daily study hours (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Double avgDailyHours = progressRepository
                .getAverageDailyStudyHours(userId, thirtyDaysAgo);
        progress.put("avgDailyStudyHours",
                avgDailyHours != null ? Math.round(avgDailyHours * 10.0) / 10.0 : 0.0);

        // Total answers practiced
        Long totalAnswers = progressRepository.getTotalAnswersPracticed(userId);
        progress.put("totalAnswersPracticed", totalAnswers != null ? totalAnswers : 0L);

        // Total quizzes from progress records
        Long totalQuizzesCompleted = progressRepository.getTotalQuizzesCompleted(userId);
        progress.put("totalQuizzesFromProgress",
                totalQuizzesCompleted != null ? totalQuizzesCompleted : 0L);

        // Study hours by subject
        List<Object[]> hoursBySubject = progressRepository.getStudyHoursBySubject(userId);
        Map<String, Double> subjectHours = new LinkedHashMap<>();
        for (Object[] row : hoursBySubject) {
            if (row[0] != null && row[1] != null) {
                Subject subject = (Subject) row[0];
                Double hours = ((Number) row[1]).doubleValue();
                subjectHours.put(subject.getDisplayName(), Math.round(hours * 10.0) / 10.0);
            }
        }
        progress.put("studyHoursBySubject", subjectHours);

        // Daily study hours trend (last 14 days for chart)
        LocalDate fourteenDaysAgo = LocalDate.now().minusDays(14);
        List<Object[]> dailyHoursData = progressRepository
                .getDailyStudyHoursForPeriod(userId, fourteenDaysAgo);
        List<Map<String, Object>> dailyTrend = new ArrayList<>();
        for (Object[] row : dailyHoursData) {
            if (row[0] != null && row[1] != null) {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("date", row[0].toString());
                point.put("hours", ((Number) row[1]).doubleValue());
                dailyTrend.add(point);
            }
        }
        progress.put("dailyStudyHoursTrend", dailyTrend);

        return progress;
    }

    // ==========================================================
    //  STREAK DATA
    // ==========================================================

    private Map<String, Object> getStreakData(Long userId) {
        Map<String, Object> streakData = new LinkedHashMap<>();

        int currentStreak = calculateCurrentStreak(userId);
        int longestStreak = calculateLongestStreak(userId);

        streakData.put("currentStreak", currentStreak);
        streakData.put("longestStreak", longestStreak);

        // Streak status message
        String streakMessage;
        if (currentStreak == 0) {
            streakMessage = "Start studying today to begin your streak! ðŸ’ª";
        } else if (currentStreak < 7) {
            streakMessage = "Keep going! " + currentStreak + " days strong! ðŸ”¥";
        } else if (currentStreak < 30) {
            streakMessage = "Amazing! " + currentStreak + " days streak! You're building momentum! âš¡";
        } else if (currentStreak < 100) {
            streakMessage = "Incredible! " + currentStreak + " days! You're a true warrior! ðŸ†";
        } else {
            streakMessage = "Legendary! " + currentStreak + " days streak! UPSC is yours! ðŸ‘‘";
        }
        streakData.put("streakMessage", streakMessage);

        // Did user study today?
        boolean studiedToday = progressRepository
                .existsByUserIdAndRecordDate(userId, LocalDate.now());
        streakData.put("studiedToday", studiedToday);

        return streakData;
    }

    /**
     * Calculate current study streak (consecutive days)
     */
    public int calculateCurrentStreak(Long userId) {
        List<LocalDate> studyDates = progressRepository
                .getStudyDatesDescending(userId);

        if (studyDates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Check if studied today or yesterday (to not break streak mid-day)
        LocalDate firstDate = studyDates.get(0);
        if (!firstDate.equals(today) && !firstDate.equals(yesterday)) {
            return 0; // Streak is broken
        }

        int streak = 1;
        for (int i = 1; i < studyDates.size(); i++) {
            LocalDate current = studyDates.get(i);
            LocalDate previous = studyDates.get(i - 1);

            // Check if dates are consecutive
            if (previous.minusDays(1).equals(current)) {
                streak++;
            } else if (!previous.equals(current)) {
                // Not the same day and not consecutive -> streak ends
                break;
            }
            // If same day (duplicate entries), skip and continue
        }

        return streak;
    }

    /**
     * Calculate longest streak ever
     */
    public int calculateLongestStreak(Long userId) {
        List<LocalDate> studyDates = progressRepository
                .getStudyDatesDescending(userId);

        if (studyDates.isEmpty()) return 0;

        // Get unique dates in ascending order
        List<LocalDate> uniqueDatesAsc = studyDates.stream()
                .distinct()
                .sorted()
                .toList();

        if (uniqueDatesAsc.size() == 1) return 1;

        int longestStreak = 1;
        int currentStreak = 1;

        for (int i = 1; i < uniqueDatesAsc.size(); i++) {
            if (uniqueDatesAsc.get(i).equals(uniqueDatesAsc.get(i - 1).plusDays(1))) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }

        return longestStreak;
    }

    // ==========================================================
    //  STUDY PLAN STATUS
    // ==========================================================

    private Map<String, Object> getStudyPlanStatus(Long userId) {
        Map<String, Object> planStatus = new LinkedHashMap<>();

        try {
            // Get active plans
            List<StudyPlan> activePlans = studyPlanRepository
                    .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);

            planStatus.put("hasActivePlan", !activePlans.isEmpty());
            planStatus.put("totalActivePlans", activePlans.size());

            if (!activePlans.isEmpty()) {
                StudyPlan latestPlan = activePlans.get(0);
                planStatus.put("currentPlanTitle", latestPlan.getPlanTitle());
                planStatus.put("currentPhase", latestPlan.getCurrentPhaseDisplay());
                planStatus.put("planCompletion",
                        latestPlan.getCompletionPercentage() != null
                                ? latestPlan.getCompletionPercentage()
                                : 0.0);
                planStatus.put("daysRemaining", latestPlan.getDaysRemaining());
                planStatus.put("planStatus", latestPlan.getStatusLabel());
                planStatus.put("needsAttention", latestPlan.needsAttention());
                planStatus.put("nextMilestone", latestPlan.getNextMilestone());
                planStatus.put("nextMilestoneDate", latestPlan.getNextMilestoneDate());
            } else {
                planStatus.put("currentPlanTitle", "No active plan");
                planStatus.put("currentPhase", "Not Started");
                planStatus.put("planCompletion", 0.0);
                planStatus.put("daysRemaining", -1L);
                planStatus.put("planStatus", "ðŸ“‹ Create a Plan");
                planStatus.put("needsAttention", false);
                planStatus.put("nextMilestone", "Create your first study plan!");
                planStatus.put("nextMilestoneDate", null);
            }

            // Average completion across all active plans
            Double avgCompletion = studyPlanRepository.getAverageCompletion(userId);
            planStatus.put("averageCompletion",
                    avgCompletion != null ? Math.round(avgCompletion * 10.0) / 10.0 : 0.0);

            // Completed plans count
            Long completedPlans = studyPlanRepository.countCompletedPlans(userId);
            planStatus.put("completedPlans", completedPlans != null ? completedPlans : 0L);

        } catch (Exception e) {
            logger.warn("Error loading study plan status: {}", e.getMessage());
            planStatus.put("hasActivePlan", false);
            planStatus.put("totalActivePlans", 0);
            planStatus.put("error", "Could not load plan status");
        }

        return planStatus;
    }

    // ==========================================================
    //  SUBJECT STRENGTH ANALYSIS
    // ==========================================================

    private Map<String, Object> analyzeSubjectStrengths(Map<String, Double> subjectScores) {
        Map<String, Object> analysis = new LinkedHashMap<>();

        List<String> weakSubjects = new ArrayList<>();
        List<String> strongSubjects = new ArrayList<>();
        List<String> averageSubjects = new ArrayList<>();

        for (Map.Entry<String, Double> entry : subjectScores.entrySet()) {
            double score = entry.getValue();
            if (score >= 70.0) {
                strongSubjects.add(entry.getKey() + " (" + Math.round(score) + "%)");
            } else if (score >= 50.0) {
                averageSubjects.add(entry.getKey() + " (" + Math.round(score) + "%)");
            } else {
                weakSubjects.add(entry.getKey() + " (" + Math.round(score) + "%)");
            }
        }

        analysis.put("strongSubjects", strongSubjects);
        analysis.put("averageSubjects", averageSubjects);
        analysis.put("weakSubjects", weakSubjects);
        analysis.put("totalSubjectsAttempted", subjectScores.size());

        // Recommendation
        if (weakSubjects.isEmpty() && !strongSubjects.isEmpty()) {
            analysis.put("recommendation",
                    "ðŸ† Great progress! Focus on maintaining consistency.");
        } else if (!weakSubjects.isEmpty()) {
            analysis.put("recommendation",
                    "âš ï¸ Focus more on: " + String.join(", ",
                            weakSubjects.stream().limit(3).toList()));
        } else {
            analysis.put("recommendation",
                    "ðŸ“š Take more quizzes to track your subject-wise progress.");
        }

        return analysis;
    }

    // ==========================================================
    //  RECENT ACTIVITY
    // ==========================================================

    private Map<String, Object> getRecentActivity(Long userId) {
        Map<String, Object> activity = new LinkedHashMap<>();

        // Recent quiz results (last 5)
        List<QuizResult> recentQuizzes = quizResultRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .limit(5)
                .toList();

        List<Map<String, Object>> recentQuizList = recentQuizzes.stream()
                .map(q -> {
                    Map<String, Object> quiz = new LinkedHashMap<>();
                    quiz.put("type", "QUIZ");
                    quiz.put("subject", q.getSubject() != null
                            ? q.getSubject().getDisplayName() : "General");
                    quiz.put("score", q.getScorePercentage());
                    quiz.put("detail", q.getCorrectAnswers() + "/" + q.getTotalQuestions());
                    quiz.put("date", q.getCreatedAt());
                    quiz.put("icon", "â“");
                    return quiz;
                })
                .collect(Collectors.toList());
        activity.put("recentQuizzes", recentQuizList);

        // Recent chat subjects (last 5 unique sessions)
        var recentChats = chatHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .filter(ch -> "USER".equals(ch.getRole()))
                .limit(10)
                .map(ch -> {
                    Map<String, Object> chat = new LinkedHashMap<>();
                    chat.put("type", "CHAT");
                    chat.put("subject", ch.getSubject() != null
                            ? ch.getSubject().getDisplayName() : "General");
                    chat.put("preview", ch.getContent().length() > 50
                            ? ch.getContent().substring(0, 50) + "..."
                            : ch.getContent());
                    chat.put("date", ch.getCreatedAt());
                    chat.put("icon", "ðŸ’¬");
                    return chat;
                })
                .limit(5)
                .collect(Collectors.toList());
        activity.put("recentChats", recentChats);

        // Recent progress records (last 5)
        List<ProgressRecord> recentProgress = progressRepository
                .findByUserIdOrderByRecordDateDesc(userId)
                .stream()
                .limit(5)
                .toList();

        List<Map<String, Object>> recentProgressList = recentProgress.stream()
                .map(p -> {
                    Map<String, Object> prog = new LinkedHashMap<>();
                    prog.put("type", "STUDY");
                    prog.put("subject", p.getSubject() != null
                            ? p.getSubject().getDisplayName() : "General");
                    prog.put("hours", p.getStudyHours());
                    prog.put("date", p.getRecordDate());
                    prog.put("topics", p.getTopicsCovered());
                    prog.put("icon", "ðŸ“–");
                    return prog;
                })
                .collect(Collectors.toList());
        activity.put("recentStudySessions", recentProgressList);

        // Combine and sort all activities by date
        List<Map<String, Object>> allActivities = new ArrayList<>();
        allActivities.addAll(recentQuizList);
        allActivities.addAll(recentChats);
        allActivities.addAll(recentProgressList);
        allActivities.sort((a, b) -> {
            Object dateA = a.get("date");
            Object dateB = b.get("date");
            if (dateA instanceof LocalDateTime dtA && dateB instanceof LocalDateTime dtB) {
                return dtB.compareTo(dtA);
            }
            return 0;
        });
        activity.put("timeline", allActivities.stream().limit(10).toList());

        return activity;
    }

    // ==========================================================
    //  EXAM COUNTDOWN
    // ==========================================================

    private Map<String, Object> getExamCountdown(Integer targetYear) {
        Map<String, Object> countdown = new LinkedHashMap<>();

        int year = targetYear != null ? targetYear : LocalDate.now().getYear() + 1;

        // Approximate UPSC dates
        LocalDate prelimsDate = LocalDate.of(year, 5, 25);  // Usually last Sunday of May
        LocalDate mainsDate = LocalDate.of(year, 9, 15);     // Usually mid September
        LocalDate interviewDate = LocalDate.of(year + 1, 2, 1); // Usually Feb-March

        LocalDate today = LocalDate.now();

        // Days remaining
        long daysToPrelims = Math.max(0, prelimsDate.toEpochDay() - today.toEpochDay());
        long daysToMains = Math.max(0, mainsDate.toEpochDay() - today.toEpochDay());
        long daysToInterview = Math.max(0, interviewDate.toEpochDay() - today.toEpochDay());

        countdown.put("targetYear", year);
        countdown.put("daysToPrelims", daysToPrelims);
        countdown.put("daysToMains", daysToMains);
        countdown.put("daysToInterview", daysToInterview);
        countdown.put("prelimsDate", prelimsDate.toString());
        countdown.put("mainsDate", mainsDate.toString());
        countdown.put("interviewDate", interviewDate.toString());

        // Weeks remaining
        countdown.put("weeksToPrelims", daysToPrelims / 7);
        countdown.put("weeksToMains", daysToMains / 7);

        // Months remaining
        countdown.put("monthsToPrelims", daysToPrelims / 30);
        countdown.put("monthsToMains", daysToMains / 30);

        // Urgency level
        String urgency;
        if (daysToPrelims <= 30) {
            urgency = "ðŸ”´ CRITICAL - Less than 1 month to Prelims!";
        } else if (daysToPrelims <= 90) {
            urgency = "ðŸŸ  HIGH - Less than 3 months to Prelims!";
        } else if (daysToPrelims <= 180) {
            urgency = "ðŸŸ¡ MODERATE - 6 months to go. Stay consistent!";
        } else {
            urgency = "ðŸŸ¢ COMFORTABLE - Good time to build strong foundation!";
        }
        countdown.put("urgency", urgency);

        // Next upcoming exam
        if (daysToPrelims > 0) {
            countdown.put("nextExam", "Prelims");
            countdown.put("nextExamDays", daysToPrelims);
            countdown.put("nextExamDate", prelimsDate.toString());
        } else if (daysToMains > 0) {
            countdown.put("nextExam", "Mains");
            countdown.put("nextExamDays", daysToMains);
            countdown.put("nextExamDate", mainsDate.toString());
        } else if (daysToInterview > 0) {
            countdown.put("nextExam", "Interview");
            countdown.put("nextExamDays", daysToInterview);
            countdown.put("nextExamDate", interviewDate.toString());
        } else {
            countdown.put("nextExam", "Results");
            countdown.put("nextExamDays", 0);
            countdown.put("nextExamDate", "Completed");
        }

        return countdown;
    }

    // ==========================================================
    //  TODAY'S SUMMARY
    // ==========================================================

    private Map<String, Object> getTodaySummary(Long userId) {
        Map<String, Object> today = new LinkedHashMap<>();

        LocalDate todayDate = LocalDate.now();

        // Study records for today
        List<ProgressRecord> todayRecords = progressRepository
                .findByUserIdAndRecordDateOrderBySubject(userId, todayDate);

        double todayHours = todayRecords.stream()
                .filter(r -> r.getStudyHours() != null)
                .mapToDouble(ProgressRecord::getStudyHours)
                .sum();

        int todayAnswers = todayRecords.stream()
                .filter(r -> r.getAnswersPracticed() != null)
                .mapToInt(ProgressRecord::getAnswersPracticed)
                .sum();

        int todayQuizzes = todayRecords.stream()
                .filter(r -> r.getQuizzesCompleted() != null)
                .mapToInt(ProgressRecord::getQuizzesCompleted)
                .sum();

        List<String> todaySubjects = todayRecords.stream()
                .filter(r -> r.getSubject() != null)
                .map(r -> r.getSubject().getDisplayName())
                .distinct()
                .collect(Collectors.toList());

        List<String> todayTopics = todayRecords.stream()
                .filter(r -> r.getTopicsCovered() != null && !r.getTopicsCovered().isEmpty())
                .map(ProgressRecord::getTopicsCovered)
                .collect(Collectors.toList());

        today.put("studyHours", Math.round(todayHours * 10.0) / 10.0);
        today.put("answersPracticed", todayAnswers);
        today.put("quizzesCompleted", todayQuizzes);
        today.put("subjectsStudied", todaySubjects);
        today.put("topicsCovered", todayTopics);
        today.put("totalActivities", todayRecords.size());

        // Target comparison
        User user = userService.getUserById(userId);
        int targetHours = user.getDailyStudyHours() != null ? user.getDailyStudyHours() : 8;
        today.put("targetHours", targetHours);
        today.put("hoursRemaining", Math.max(0, targetHours - todayHours));
        today.put("targetMet", todayHours >= targetHours);

        // Progress percentage for today
        double todayProgress = Math.min(100.0, (todayHours / targetHours) * 100);
        today.put("progressPercentage", Math.round(todayProgress * 10.0) / 10.0);

        // Motivational message
        String message;
        if (todayHours == 0) {
            message = "ðŸ“š Start your study session for today!";
        } else if (todayHours < targetHours * 0.5) {
            message = "ðŸ”¥ Good start! Keep going to reach your daily target!";
        } else if (todayHours < targetHours) {
            message = "ðŸ’ª Almost there! " +
                    Math.round(targetHours - todayHours) + " more hours to hit your target!";
        } else {
            message = "ðŸ† Daily target achieved! Great discipline!";
        }
        today.put("motivationMessage", message);

        return today;
    }

    // ==========================================================
    //  WEEKLY SUMMARY
    // ==========================================================

    private Map<String, Object> getWeeklySummary(Long userId) {
        Map<String, Object> weekly = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1); // Monday
        LocalDate weekEnd = today;

        // Progress records for this week
        List<ProgressRecord> weekRecords = progressRepository
                .findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                        userId, weekStart, weekEnd);

        // Total hours this week
        double weeklyHours = weekRecords.stream()
                .filter(r -> r.getStudyHours() != null)
                .mapToDouble(ProgressRecord::getStudyHours)
                .sum();

        // Answers this week
        int weeklyAnswers = weekRecords.stream()
                .filter(r -> r.getAnswersPracticed() != null)
                .mapToInt(ProgressRecord::getAnswersPracticed)
                .sum();

        // Quizzes this week
        int weeklyQuizzes = weekRecords.stream()
                .filter(r -> r.getQuizzesCompleted() != null)
                .mapToInt(ProgressRecord::getQuizzesCompleted)
                .sum();

        // Days studied this week
        long daysStudied = weekRecords.stream()
                .map(ProgressRecord::getRecordDate)
                .distinct()
                .count();

        // Subjects covered this week
        List<String> weeklySubjects = weekRecords.stream()
                .filter(r -> r.getSubject() != null)
                .map(r -> r.getSubject().getDisplayName())
                .distinct()
                .collect(Collectors.toList());

        // Daily breakdown for the week
        Map<String, Double> dailyBreakdown = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            String dayName = date.getDayOfWeek().toString().substring(0, 3);

            double dayHours = weekRecords.stream()
                    .filter(r -> r.getRecordDate().equals(date) && r.getStudyHours() != null)
                    .mapToDouble(ProgressRecord::getStudyHours)
                    .sum();

            dailyBreakdown.put(dayName, Math.round(dayHours * 10.0) / 10.0);
        }

        // Weekly target
        User user = userService.getUserById(userId);
        int dailyTarget = user.getDailyStudyHours() != null ? user.getDailyStudyHours() : 8;
        int weeklyTarget = dailyTarget * 7;

        weekly.put("totalHours", Math.round(weeklyHours * 10.0) / 10.0);
        weekly.put("totalAnswers", weeklyAnswers);
        weekly.put("totalQuizzes", weeklyQuizzes);
        weekly.put("daysStudied", daysStudied);
        weekly.put("subjectsCovered", weeklySubjects);
        weekly.put("dailyBreakdown", dailyBreakdown);
        weekly.put("weeklyTarget", weeklyTarget);
        weekly.put("weekStart", weekStart.toString());
        weekly.put("weekEnd", weekEnd.toString());
        weekly.put("completionPercentage",
                Math.min(100.0, Math.round(weeklyHours / weeklyTarget * 100 * 10.0) / 10.0));

        // Comparison with last week
        LocalDate lastWeekStart = weekStart.minusDays(7);
        LocalDate lastWeekEnd = weekStart.minusDays(1);
        List<ProgressRecord> lastWeekRecords = progressRepository
                .findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(
                        userId, lastWeekStart, lastWeekEnd);

        double lastWeekHours = lastWeekRecords.stream()
                .filter(r -> r.getStudyHours() != null)
                .mapToDouble(ProgressRecord::getStudyHours)
                .sum();

        double weekOverWeekChange = weeklyHours - lastWeekHours;
        weekly.put("lastWeekHours", Math.round(lastWeekHours * 10.0) / 10.0);
        weekly.put("weekOverWeekChange", Math.round(weekOverWeekChange * 10.0) / 10.0);
        weekly.put("isImproving", weekOverWeekChange > 0);

        String weeklyMessage;
        if (weekOverWeekChange > 5) {
            weeklyMessage = "ðŸ“ˆ Excellent! You're studying " +
                    Math.abs(Math.round(weekOverWeekChange)) + " hours more than last week!";
        } else if (weekOverWeekChange > 0) {
            weeklyMessage = "ðŸ‘ Good improvement over last week!";
        } else if (weekOverWeekChange == 0) {
            weeklyMessage = "ðŸ“Š Consistent with last week. Try to push a bit more!";
        } else {
            weeklyMessage = "âš ï¸ You're " +
                    Math.abs(Math.round(weekOverWeekChange)) +
                    " hours behind last week. Let's catch up!";
        }
        weekly.put("weeklyMessage", weeklyMessage);

        return weekly;
    }

    // ==========================================================
    //  AI PROGRESS ANALYSIS
    // ==========================================================

    /**
     * Generate AI-powered progress analysis and recommendations
     */
    public String generateProgressAnalysis(Long userId) {
        User user = userService.getUserById(userId);
        Map<String, Object> dashboard = getDashboardData(userId);

        String prompt = """
                Analyze the following UPSC aspirant's comprehensive progress data
                and provide detailed, personalized recommendations.
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                STUDENT PROFILE
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                - Name: %s
                - Target Year: %s
                - Current Level: %s
                - Optional Subject: %s
                - Attempt Number: %s
                - Daily Study Target: %s hours
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                QUIZ PERFORMANCE
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                - Total Quizzes Taken: %s
                - Average Score: %s%%
                - Subject-wise Scores: %s
                - Score Trend: %s
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                STUDY METRICS
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                - Total Study Hours: %s
                - Total Study Days: %s
                - Average Daily Hours (30 days): %s
                - Total Answers Practiced: %s
                - Current Streak: %s days
                - Longest Streak: %s days
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                SUBJECT ANALYSIS
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                - Weak Subjects (from quizzes): %s
                - Strong Subjects (from quizzes): %s
                - Self-identified Weak Areas: %s
                - Study Hours by Subject: %s
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                WEEKLY SUMMARY
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                %s
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                EXAM COUNTDOWN
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                %s
                
                â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                
                Based on this data, provide a COMPREHENSIVE analysis:
                
                ## ðŸ“Š Overall Assessment
                - Where does the student currently stand?
                - Rate preparedness on a scale of 1-10
                
                ## âœ… Strengths
                - What's going well?
                - Which subjects are on track?
                
                ## âš ï¸ Areas of Concern
                - What needs immediate attention?
                - Which subjects are lagging?
                
                ## ðŸ“‹ Specific Recommendations
                1. Subject-wise action items
                2. Study habit improvements
                3. Time management suggestions
                4. Answer writing improvement tips
                
                ## ðŸŽ¯ Priority Changes (Next 2 Weeks)
                - Top 5 actionable steps
                
                ## ðŸ“ˆ Performance Prediction
                - Based on current trajectory, prediction for exam readiness
                - What needs to change to improve chances
                
                ## ðŸ’ª Motivation
                - Encouraging words based on actual progress
                - Comparison with typical successful candidates
                
                Keep the analysis specific, data-driven, and actionable.
                Use the actual numbers provided in your recommendations.
                """.formatted(
                dashboard.get("userName"),
                dashboard.get("targetYear"),
                dashboard.get("level"),
                dashboard.get("optionalSubject"),
                dashboard.get("attemptNumber"),
                dashboard.get("dailyStudyHoursTarget"),
                dashboard.get("totalQuizzes"),
                dashboard.get("averageScore"),
                dashboard.get("subjectScores"),
                ((Map<?, ?>) dashboard.get("quizStats")).get("trendDirection"),
                ((Map<?, ?>) dashboard.get("studyProgress")).get("totalStudyHours"),
                ((Map<?, ?>) dashboard.get("studyProgress")).get("totalStudyDays"),
                ((Map<?, ?>) dashboard.get("studyProgress")).get("avgDailyStudyHours"),
                ((Map<?, ?>) dashboard.get("studyProgress")).get("totalAnswersPracticed"),
                dashboard.get("currentStreak"),
                dashboard.get("longestStreak"),
                dashboard.get("weakSubjectsFromQuiz"),
                dashboard.get("strongSubjectsFromQuiz"),
                dashboard.get("weakSubjects"),
                ((Map<?, ?>) dashboard.get("studyProgress")).get("studyHoursBySubject"),
                dashboard.get("weeklySummary"),
                dashboard.get("examCountdown")
        );

        try {
            logger.info("Generating AI progress analysis for user ID: {}", userId);
            return aiModelRouterService.generate(user, prompt);
        } catch (Exception e) {
            logger.error("Error generating progress analysis: {}", e.getMessage());
            return "Unable to generate analysis at this time. Please check AI configuration and try again.";
        }
    }

    // ==========================================================
    //  HELPER METHODS
    // ==========================================================

    /**
     * Calculate if scores are improving, declining, or stable
     */
    private String calculateTrend(List<Double> scores) {
        if (scores == null || scores.size() < 3) return "INSUFFICIENT_DATA";

        // Compare average of first half vs second half
        int mid = scores.size() / 2;

        double firstHalfAvg = scores.subList(0, mid).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double secondHalfAvg = scores.subList(mid, scores.size()).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double difference = secondHalfAvg - firstHalfAvg;

        if (difference > 5) return "ðŸ“ˆ IMPROVING";
        if (difference < -5) return "ðŸ“‰ DECLINING";
        return "âž¡ï¸ STABLE";
    }

    /**
     * Record a study session (helper method for other services)
     */
    public void recordStudySession(Long userId, Subject subject,
                                   double hours, String topicsCovered) {
        LocalDate today = LocalDate.now();

        Optional<ProgressRecord> existingRecord = progressRepository
                .findByUserIdAndRecordDateAndSubject(userId, today, subject);

        ProgressRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
            record.addStudyHours(hours);
            if (topicsCovered != null) {
                record.addTopicCovered(topicsCovered);
            }
        } else {
            record = new ProgressRecord(userId, subject);
            record.setStudyHours(hours);
            record.setTopicsCovered(topicsCovered);
        }

        progressRepository.save(record);
        logger.debug("Recorded study session: {} hours for {} - {}",
                hours, subject.getDisplayName(), topicsCovered);
    }

    /**
     * Record a quiz completion
     */
    public void recordQuizCompletion(Long userId, Subject subject, double score) {
        LocalDate today = LocalDate.now();

        Optional<ProgressRecord> existingRecord = progressRepository
                .findByUserIdAndRecordDateAndSubject(userId, today, subject);

        ProgressRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
        } else {
            record = new ProgressRecord(userId, subject);
        }

        record.incrementQuizzesCompleted();
        record.setQuizScore(score);
        progressRepository.save(record);
    }

    /**
     * Record answer writing practice
     */
    public void recordAnswerPractice(Long userId, Subject subject) {
        LocalDate today = LocalDate.now();

        Optional<ProgressRecord> existingRecord = progressRepository
                .findByUserIdAndRecordDateAndSubject(userId, today, subject);

        ProgressRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
        } else {
            record = new ProgressRecord(userId, subject);
        }

        record.incrementAnswersPracticed();
        progressRepository.save(record);
    }
}
