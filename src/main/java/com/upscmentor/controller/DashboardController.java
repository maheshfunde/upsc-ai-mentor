package com.upscmentor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/onboarding")
    public String onboarding() {
        return "onboarding";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/quiz")
    public String quiz() {
        return "quiz";
    }

    @GetMapping("/answer-practice")
    public String answerPractice() {
        return "answer-practice";
    }

    @GetMapping("/study-plan")
    public String studyPlan() {
        return "study-plan";
    }

    @GetMapping("/current-affairs")
    public String currentAffairs() {
        return "current-affairs";
    }

    @GetMapping("/pyq-analysis")
    public String pyqAnalysis() {
        return "pyq-analysis";
    }

    @GetMapping("/ai-settings")
    public String aiSettings() {
        return "ai-settings";
    }

    @GetMapping("/reference")
    public String quickReference() {
        return "reference";
    }

    @GetMapping("/flashcards")
    public String flashcards() {
        return "flashcards";
    }
}
