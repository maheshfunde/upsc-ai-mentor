package com.upscmentor.controller;

import com.upscmentor.service.StudyPlanService;
import com.upscmentor.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/study-plan")
@CrossOrigin(origins = "*")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final ProgressService progressService;

    public StudyPlanController(StudyPlanService studyPlanService,
                               ProgressService progressService) {
        this.studyPlanService = studyPlanService;
        this.progressService = progressService;
    }

    /**
     * GET /api/study-plan/generate/{userId} - Generate personalized study plan
     */
    @GetMapping("/generate/{userId}")
    public ResponseEntity<Map<String, Object>> generateStudyPlan(@PathVariable Long userId) {
        String plan = studyPlanService.generateStudyPlan(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "plan", plan
        ));
    }

    /**
     * GET /api/study-plan/daily/{userId} - Generate daily schedule
     */
    @GetMapping("/daily/{userId}")
    public ResponseEntity<Map<String, Object>> getDailySchedule(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "General Studies") String focusSubject) {

        String schedule = studyPlanService.generateDailySchedule(userId, focusSubject);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "schedule", schedule
        ));
    }

    /**
     * GET /api/study-plan/weekly/{userId} - Generate weekly revision plan
     */
    @GetMapping("/weekly/{userId}")
    public ResponseEntity<Map<String, Object>> getWeeklyPlan(@PathVariable Long userId) {
        String plan = studyPlanService.generateWeeklyRevisionPlan(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "plan", plan
        ));
    }

    /**
     * GET /api/study-plan/progress/{userId} - Get progress dashboard
     */
    @GetMapping("/progress/{userId}")
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable Long userId) {
        return ResponseEntity.ok(progressService.getDashboardData(userId));
    }

    /**
     * GET /api/study-plan/analysis/{userId} - Get AI progress analysis
     */
    @GetMapping("/analysis/{userId}")
    public ResponseEntity<Map<String, Object>> getProgressAnalysis(@PathVariable Long userId) {
        String analysis = progressService.generateProgressAnalysis(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "analysis", analysis
        ));
    }
}