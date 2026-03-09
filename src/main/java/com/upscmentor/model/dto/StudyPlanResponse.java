package com.upscmentor.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class StudyPlanResponse {

    private boolean success;
    private String error;
    private LocalDateTime generatedAt;
    private String planType;  // FULL, DAILY, WEEKLY, MONTHLY
    private StudyPlanData planData;

    // ===== Inner Classes =====

    /**
     * Complete study plan data
     */
    public static class StudyPlanData {
        private String studentName;
        private String optionalSubject;
        private String difficultyLevel;
        private int targetYear;
        private String rawPlanContent;  // AI-generated markdown content
        private PlanOverview overview;
        private List<PlanPhase> phases;
        private DailySchedule dailyScheduleTemplate;
        private WeeklyPlan weeklyPlan;
        private List<SubjectStrategy> subjectStrategies;
        private List<String> recommendedBooks;
        private List<String> milestones;

        // Getters and Setters
        public String getStudentName() { return studentName; }
        public void setStudentName(String studentName) { this.studentName = studentName; }
        public String getOptionalSubject() { return optionalSubject; }
        public void setOptionalSubject(String optionalSubject) { this.optionalSubject = optionalSubject; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public int getTargetYear() { return targetYear; }
        public void setTargetYear(int targetYear) { this.targetYear = targetYear; }
        public String getRawPlanContent() { return rawPlanContent; }
        public void setRawPlanContent(String rawPlanContent) { this.rawPlanContent = rawPlanContent; }
        public PlanOverview getOverview() { return overview; }
        public void setOverview(PlanOverview overview) { this.overview = overview; }
        public List<PlanPhase> getPhases() { return phases; }
        public void setPhases(List<PlanPhase> phases) { this.phases = phases; }
        public DailySchedule getDailyScheduleTemplate() { return dailyScheduleTemplate; }
        public void setDailyScheduleTemplate(DailySchedule dailyScheduleTemplate) { this.dailyScheduleTemplate = dailyScheduleTemplate; }
        public WeeklyPlan getWeeklyPlan() { return weeklyPlan; }
        public void setWeeklyPlan(WeeklyPlan weeklyPlan) { this.weeklyPlan = weeklyPlan; }
        public List<SubjectStrategy> getSubjectStrategies() { return subjectStrategies; }
        public void setSubjectStrategies(List<SubjectStrategy> subjectStrategies) { this.subjectStrategies = subjectStrategies; }
        public List<String> getRecommendedBooks() { return recommendedBooks; }
        public void setRecommendedBooks(List<String> recommendedBooks) { this.recommendedBooks = recommendedBooks; }
        public List<String> getMilestones() { return milestones; }
        public void setMilestones(List<String> milestones) { this.milestones = milestones; }
    }

    /**
     * Overview of the study plan
     */
    public static class PlanOverview {
        private int totalMonths;
        private int totalWeeks;
        private LocalDate startDate;
        private LocalDate examDate;
        private int dailyStudyHours;
        private String planSummary;

        // Getters and Setters
        public int getTotalMonths() { return totalMonths; }
        public void setTotalMonths(int totalMonths) { this.totalMonths = totalMonths; }
        public int getTotalWeeks() { return totalWeeks; }
        public void setTotalWeeks(int totalWeeks) { this.totalWeeks = totalWeeks; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getExamDate() { return examDate; }
        public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
        public int getDailyStudyHours() { return dailyStudyHours; }
        public void setDailyStudyHours(int dailyStudyHours) { this.dailyStudyHours = dailyStudyHours; }
        public String getPlanSummary() { return planSummary; }
        public void setPlanSummary(String planSummary) { this.planSummary = planSummary; }
    }

    /**
     * A phase in the study plan (e.g., Foundation, Advanced, Revision)
     */
    public static class PlanPhase {
        private String phaseName;        // "Foundation Phase"
        private int phaseNumber;         // 1, 2, 3
        private String duration;         // "3 months"
        private LocalDate startDate;
        private LocalDate endDate;
        private String objective;
        private List<String> subjects;   // Subjects to cover
        private List<String> activities; // Activities for this phase
        private String milestone;        // End-of-phase milestone

        // Getters and Setters
        public String getPhaseName() { return phaseName; }
        public void setPhaseName(String phaseName) { this.phaseName = phaseName; }
        public int getPhaseNumber() { return phaseNumber; }
        public void setPhaseNumber(int phaseNumber) { this.phaseNumber = phaseNumber; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getObjective() { return objective; }
        public void setObjective(String objective) { this.objective = objective; }
        public List<String> getSubjects() { return subjects; }
        public void setSubjects(List<String> subjects) { this.subjects = subjects; }
        public List<String> getActivities() { return activities; }
        public void setActivities(List<String> activities) { this.activities = activities; }
        public String getMilestone() { return milestone; }
        public void setMilestone(String milestone) { this.milestone = milestone; }
    }

    /**
     * Daily schedule template
     */
    public static class DailySchedule {
        private List<TimeSlot> timeSlots;
        private String focusSubject;
        private int totalHours;

        // Getters and Setters
        public List<TimeSlot> getTimeSlots() { return timeSlots; }
        public void setTimeSlots(List<TimeSlot> timeSlots) { this.timeSlots = timeSlots; }
        public String getFocusSubject() { return focusSubject; }
        public void setFocusSubject(String focusSubject) { this.focusSubject = focusSubject; }
        public int getTotalHours() { return totalHours; }
        public void setTotalHours(int totalHours) { this.totalHours = totalHours; }
    }

    /**
     * A time slot in daily schedule
     */
    public static class TimeSlot {
        private String startTime;    // "06:00"
        private String endTime;      // "07:30"
        private String activity;     // "Static GS - Polity"
        private String category;     // "STUDY", "BREAK", "REVISION", "EXERCISE"
        private String notes;

        // Constructors
        public TimeSlot() {}

        public TimeSlot(String startTime, String endTime, String activity, String category) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.activity = activity;
            this.category = category;
        }

        // Getters and Setters
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * Weekly plan structure
     */
    public static class WeeklyPlan {
        private Map<String, DayPlan> days; // "Monday" -> DayPlan

        // Getters and Setters
        public Map<String, DayPlan> getDays() { return days; }
        public void setDays(Map<String, DayPlan> days) { this.days = days; }
    }

    /**
     * Plan for a single day in weekly plan
     */
    public static class DayPlan {
        private String dayName;
        private List<String> subjects;
        private List<String> topics;
        private String focusArea;
        private int answersToWrite;
        private boolean hasQuiz;
        private String specialActivity; // Mock test, revision, etc.

        // Getters and Setters
        public String getDayName() { return dayName; }
        public void setDayName(String dayName) { this.dayName = dayName; }
        public List<String> getSubjects() { return subjects; }
        public void setSubjects(List<String> subjects) { this.subjects = subjects; }
        public List<String> getTopics() { return topics; }
        public void setTopics(List<String> topics) { this.topics = topics; }
        public String getFocusArea() { return focusArea; }
        public void setFocusArea(String focusArea) { this.focusArea = focusArea; }
        public int getAnswersToWrite() { return answersToWrite; }
        public void setAnswersToWrite(int answersToWrite) { this.answersToWrite = answersToWrite; }
        public boolean isHasQuiz() { return hasQuiz; }
        public void setHasQuiz(boolean hasQuiz) { this.hasQuiz = hasQuiz; }
        public String getSpecialActivity() { return specialActivity; }
        public void setSpecialActivity(String specialActivity) { this.specialActivity = specialActivity; }
    }

    /**
     * Subject-specific strategy
     */
    public static class SubjectStrategy {
        private String subjectName;
        private int priorityLevel;           // 1 = High, 2 = Medium, 3 = Low
        private int allocatedHoursPerWeek;
        private List<String> books;
        private List<String> keyTopics;
        private int revisionsNeeded;
        private String strategy;             // AI-generated strategy text
        private String currentStatus;        // "Not Started", "In Progress", "Revision"
        private double completionPercentage;

        // Getters and Setters
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public int getPriorityLevel() { return priorityLevel; }
        public void setPriorityLevel(int priorityLevel) { this.priorityLevel = priorityLevel; }
        public int getAllocatedHoursPerWeek() { return allocatedHoursPerWeek; }
        public void setAllocatedHoursPerWeek(int allocatedHoursPerWeek) { this.allocatedHoursPerWeek = allocatedHoursPerWeek; }
        public List<String> getBooks() { return books; }
        public void setBooks(List<String> books) { this.books = books; }
        public List<String> getKeyTopics() { return keyTopics; }
        public void setKeyTopics(List<String> keyTopics) { this.keyTopics = keyTopics; }
        public int getRevisionsNeeded() { return revisionsNeeded; }
        public void setRevisionsNeeded(int revisionsNeeded) { this.revisionsNeeded = revisionsNeeded; }
        public String getStrategy() { return strategy; }
        public void setStrategy(String strategy) { this.strategy = strategy; }
        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
        public double getCompletionPercentage() { return completionPercentage; }
        public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }

        /**
         * Get priority label
         */
        public String getPriorityLabel() {
            return switch (priorityLevel) {
                case 1 -> "🔴 High Priority";
                case 2 -> "🟡 Medium Priority";
                case 3 -> "🟢 Low Priority";
                default -> "⚪ Not Set";
            };
        }
    }

    // ===== Static Factory Methods =====

    /**
     * Create success response with raw AI-generated content
     */
    public static StudyPlanResponse success(String planContent, String planType) {
        StudyPlanResponse response = new StudyPlanResponse();
        response.success = true;
        response.planType = planType;
        response.generatedAt = LocalDateTime.now();

        StudyPlanData data = new StudyPlanData();
        data.setRawPlanContent(planContent);
        response.planData = data;

        return response;
    }

    /**
     * Create success response with structured plan data
     */
    public static StudyPlanResponse success(StudyPlanData data, String planType) {
        StudyPlanResponse response = new StudyPlanResponse();
        response.success = true;
        response.planType = planType;
        response.generatedAt = LocalDateTime.now();
        response.planData = data;
        return response;
    }

    /**
     * Create error response
     */
    public static StudyPlanResponse error(String errorMessage) {
        StudyPlanResponse response = new StudyPlanResponse();
        response.success = false;
        response.error = errorMessage;
        response.generatedAt = LocalDateTime.now();
        return response;
    }

    // ===== Utility Methods =====

    /**
     * Create a default daily schedule template
     */
    public static DailySchedule createDefaultDailySchedule(int availableHours, String focusSubject) {
        DailySchedule schedule = new DailySchedule();
        schedule.setFocusSubject(focusSubject);
        schedule.setTotalHours(availableHours);

        List<TimeSlot> slots = List.of(
                new TimeSlot("06:00", "06:30", "🏃 Morning Exercise & Freshening Up", "EXERCISE"),
                new TimeSlot("06:30", "07:30", "📰 Newspaper Reading (The Hindu / Indian Express)", "CURRENT_AFFAIRS"),
                new TimeSlot("07:30", "08:00", "🍳 Breakfast Break", "BREAK"),
                new TimeSlot("08:00", "10:00", "📖 Static GS - " + focusSubject + " (New Topics)", "STUDY"),
                new TimeSlot("10:00", "10:15", "☕ Short Break", "BREAK"),
                new TimeSlot("10:15", "12:15", "📖 GS Continuation / Second Subject", "STUDY"),
                new TimeSlot("12:15", "13:00", "🍽️ Lunch Break", "BREAK"),
                new TimeSlot("13:00", "14:30", "📝 Answer Writing Practice (2 Answers)", "PRACTICE"),
                new TimeSlot("14:30", "14:45", "☕ Short Break", "BREAK"),
                new TimeSlot("14:45", "16:45", "📚 Optional Subject Study", "STUDY"),
                new TimeSlot("16:45", "17:15", "🍵 Tea Break + Light Walk", "BREAK"),
                new TimeSlot("17:15", "19:00", "📰 Current Affairs Notes + MCQ Practice", "REVISION"),
                new TimeSlot("19:00", "20:00", "🍽️ Dinner + Relaxation", "BREAK"),
                new TimeSlot("20:00", "21:30", "🔁 Revision of Today's Topics", "REVISION"),
                new TimeSlot("21:30", "22:00", "📋 Next Day Planning + Quick Review", "PLANNING"),
                new TimeSlot("22:00", "06:00", "😴 Sleep (8 hours)", "SLEEP")
        );

        schedule.setTimeSlots(slots);
        return schedule;
    }

    // ===== Getters and Setters =====
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getPlanType() { return planType; }
    public void setPlanType(String planType) { this.planType = planType; }
    public StudyPlanData getPlanData() { return planData; }
    public void setPlanData(StudyPlanData planData) { this.planData = planData; }
}