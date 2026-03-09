package com.upscmentor.prompts;

public class SubjectPrompts {

    /**
     * Prompt for generating quiz questions
     */
    public static String quizGenerationPrompt(String subject, String topic,
                                              int numberOfQuestions, String difficulty) {
        return """
                Generate exactly %d multiple-choice questions (MCQs) for UPSC preparation.
                
                Subject: %s
                Specific Topic: %s
                Difficulty Level: %s
                
                Format each question EXACTLY as follows (use this JSON format):
                ```json
                [
                  {
                    "question": "Question text here?",
                    "options": {
                      "A": "Option A text",
                      "B": "Option B text",
                      "C": "Option C text",
                      "D": "Option D text"
                    },
                    "correctAnswer": "B",
                    "explanation": "Detailed explanation with facts and references",
                    "upscRelevance": "This topic was asked in UPSC 20XX / frequently tested"
                  }
                ]
                ```
                
                Rules:
                - Questions should match UPSC Prelims style and difficulty
                - Include factual, conceptual, and application-based questions
                - Provide detailed explanations for each answer
                - Mention UPSC relevance or frequency if applicable
                - Make wrong options plausible (avoid obviously wrong choices)
                - Cover different aspects of the topic
                """.formatted(numberOfQuestions, subject, topic, difficulty);
    }

    /**
     * Prompt for evaluating answer writing practice
     */
    public static String answerEvaluationPrompt(String question, String userAnswer,
                                                String subject, int wordLimit) {
        return """
                You are a UPSC Mains answer evaluator. Evaluate the following answer strictly
                based on UPSC Mains standards.
                
                Subject: %s
                Question: %s
                Word Limit: %d words
                
                Student's Answer:
                ---
                %s
                ---
                
                Evaluate and respond in this EXACT JSON format:
                ```json
                {
                  "overallScore": 7,
                  "contentScore": 8,
                  "structureScore": 6,
                  "analyticalScore": 7,
                  "strengths": "What the student did well...",
                  "weaknesses": "What needs improvement...",
                  "suggestions": "Specific actionable suggestions...",
                  "modelAnswer": "A model answer for this question (within word limit)...",
                  "dimensionsMissed": "Important dimensions/perspectives the student missed..."
                }
                ```
                
                Evaluation Criteria:
                1. **Content (out of 10)**: Accuracy, depth, relevant facts, data, examples
                2. **Structure (out of 10)**: Introduction, body, conclusion, flow, word limit adherence
                3. **Analytical (out of 10)**: Critical thinking, multiple perspectives, balanced view
                4. **Overall (out of 10)**: Combined assessment
                
                UPSC-specific checks:
                - Did the student use relevant constitutional provisions/articles?
                - Were government schemes/committees mentioned?
                - Was the answer balanced (pros & cons)?
                - Were diagrams/flowcharts suggested where applicable?
                - Was the conclusion forward-looking with suggestions?
                """.formatted(subject, question, wordLimit, userAnswer);
    }

    /**
     * Prompt for generating a personalized study plan
     */
    public static String studyPlanPrompt(String name, String optionalSubject,
                                         String difficulty, int targetYear,
                                         int attemptNumber, int dailyHours,
                                         String weakSubjects, String strongSubjects) {
        return """
                Create a detailed, personalized UPSC study plan.
                
                Student Profile:
                - Name: %s
                - Optional Subject: %s
                - Current Level: %s
                - Target Year: %d
                - Attempt Number: %d
                - Available Study Hours/Day: %d
                - Weak Subjects: %s
                - Strong Subjects: %s
                
                Create a comprehensive plan that includes:
                
                1. **Phase-wise Breakdown** (Foundation → Revision → Test Series)
                2. **Monthly Schedule** with specific subjects and topics
                3. **Daily Timetable Template** based on available hours
                4. **Subject-wise Strategy**:
                   - Books to read (specific chapters)
                   - Time allocation per subject
                   - Number of revisions needed
                5. **Optional Subject Plan**: Detailed plan for %s
                6. **Answer Writing Practice Schedule**
                7. **Current Affairs Strategy** (newspapers, magazines, compilations)
                8. **Mock Test Schedule** (when to start, frequency)
                9. **Weak Subject Improvement Plan** for: %s
                10. **Weekly Review Mechanism**
                
                Important considerations:
                - Account for the attempt number (adjust intensity)
                - Include buffer days for flexibility
                - Balance static subjects with current affairs
                - Include break and recreation time
                - Add milestones and checkpoints
                """.formatted(name, optionalSubject, difficulty, targetYear,
                attemptNumber, dailyHours, weakSubjects, strongSubjects,
                optionalSubject, weakSubjects);
    }

    /**
     * Prompt for PYQ (Previous Year Question) analysis
     */
    public static String pyqAnalysisPrompt(String subject, String topic) {
        return """
                Provide a Previous Year Question (PYQ) analysis for UPSC on:
                
                Subject: %s
                Topic: %s
                
                Include:
                1. **Trend Analysis**: How frequently this topic appears in UPSC
                2. **Question Patterns**: Types of questions asked (factual, conceptual, application)
                3. **Sample PYQs**: 5 representative previous year questions
                4. **Model Answers**: Brief model answers for each
                5. **Key Takeaways**: What UPSC expects from this topic
                6. **Connected Topics**: Related topics that are often asked together
                7. **Predicted Questions**: 3 potential questions for upcoming exams
                
                Note: Frame questions in UPSC style and difficulty level.
                """.formatted(subject, topic);
    }

    /**
     * Prompt for daily current affairs digest
     */
    public static String currentAffairsPrompt(String topic) {
        return """
                As a UPSC mentor, explain the following current affairs topic for UPSC preparation:
                
                Topic: %s
                
                Cover these aspects:
                1. **What**: Brief factual summary
                2. **Why Important for UPSC**: Which paper/subject it's relevant to
                3. **Background**: Historical context and evolution
                4. **Key Points**: Important facts, data, and figures
                5. **Multiple Perspectives**: Different viewpoints on the issue
                6. **Government's Stand**: Official position, policies, schemes
                7. **Connected Static Topics**: Link to syllabus topics
                8. **Potential Questions**: How UPSC might frame questions on this
                9. **Keywords**: Important terms for answer writing
                
                Note: Mention which GS paper and subject this relates to.
                """.formatted(topic);
    }
}