package com.upscmentor.service;

import com.upscmentor.model.dto.CurrentAffairsRequest;
import com.upscmentor.model.dto.PyqAnalysisRequest;
import com.upscmentor.model.entity.User;
import com.upscmentor.prompts.SubjectPrompts;
import org.springframework.stereotype.Service;

@Service
public class ContentAnalysisService {

    private final UserService userService;
    private final AiModelRouterService aiModelRouterService;

    public ContentAnalysisService(UserService userService, AiModelRouterService aiModelRouterService) {
        this.userService = userService;
        this.aiModelRouterService = aiModelRouterService;
    }

    public String analyzeCurrentAffairs(CurrentAffairsRequest request) {
        User user = userService.getUserById(request.getUserId());

        String prompt = """
                You are a UPSC mentor. Analyze the following current affairs article/news content and produce a structured UPSC-ready note.
                Infer the topic/theme yourself from the content.

                Article/Editorial Content:
                ---
                %s
                ---

                Output format (strict markdown):
                ## Topic Inference
                ## Issue Snapshot
                ## Background and Context
                ## Key Facts and Data
                ## UPSC Syllabus Linkages (GS Papers / Topics)
                ## Stakeholders and Dimensions
                ## Constitutional / Legal / Policy Linkages
                ## PYQ Linkage Analysis
                ## Prelims Pointers
                ## Mains Answer Framework (Intro, Body points, Conclusion)
                ## Possible UPSC Questions (2 Prelims, 2 Mains)
                ## Final Revision Notes (5 bullets)
                """.formatted(request.getArticleText());

        String output = aiModelRouterService.generate(user, prompt);
        if (isLowQuality(output)) {
            String retryPrompt = prompt + """

                    Important retry instruction:
                    - Your previous output was too short or invalid.
                    - Provide full detailed markdown content under each section.
                    - Do not output single words like safe/unsafe.
                    """;
            output = aiModelRouterService.generate(user, retryPrompt);
        }
        return output;
    }

    public String analyzePyq(PyqAnalysisRequest request) {
        User user = userService.getUserById(request.getUserId());

        String base = SubjectPrompts.pyqAnalysisPrompt(request.getSubject(), request.getTopic());
        String tailored = """
                %s

                Additional instructions (strict):
                - Give at least 8 high-quality PYQs or PYQ-style questions for this topic.
                - For each Mains-style question, include a concise model answer framework.
                - For each Prelims-style MCQ, include answer and explanation.
                - Mark each question with difficulty: Easy / Moderate / UPSC-Hard.
                - Keep the content specifically curated for UPSC CSE.
                """.formatted(base);

        String output = aiModelRouterService.generate(user, tailored);
        if (isLowQuality(output)) {
            String retryPrompt = tailored + """

                    Important retry instruction:
                    - Your previous output was too short or invalid.
                    - Provide complete UPSC-ready markdown output with clear headings and model answers.
                    - Do not output single words like safe/unsafe.
                    """;
            output = aiModelRouterService.generate(user, retryPrompt);
        }
        return output;
    }

    private boolean isLowQuality(String output) {
        if (output == null) return true;
        String t = output.trim();
        if (t.length() < 80) return true;
        String lower = t.toLowerCase();
        return lower.equals("safe") || lower.equals("unsafe");
    }
}
