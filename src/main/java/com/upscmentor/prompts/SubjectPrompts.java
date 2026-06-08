package com.upscmentor.prompts;

public class SubjectPrompts {

    // ============================================
    // QUIZ GENERATION PROMPT
    // ============================================

    /**
     * Prompt for generating UPSC Prelims-style MCQ quiz questions
     */
    public static String quizGenerationPrompt(String subject, String topic,
                                              int numberOfQuestions, String difficulty) {
        return """
                Generate exactly %d UPSC Prelims-style Multiple Choice Questions (MCQs).

                Subject: %s
                Specific Topic: %s
                Difficulty Level: %s (match actual UPSC Prelims difficulty — tricky, multi-layered, not straightforward)

                QUESTION FORMAT REQUIREMENTS (UPSC Pattern 2013-2024):
                UPSC uses these MCQ patterns — vary across these types:
                1. DIRECT: Single correct factual/conceptual question
                2. STATEMENT-BASED: "Consider the following statements:" with 2-3 statements, options like "1 only", "2 only", "Both 1 and 2", "Neither 1 nor 2"
                3. "HOW MANY OF THE ABOVE": "How many of the above pairs/statements are correctly matched?" (UPSC 2023 pattern)
                4. ASSERTION-REASONING: Assertion (A) and Reason (R) with options about correctness and explanation
                5. PAIR MATCHING: 3-4 pairs (Concept-Description) — how many are correctly matched
                6. APPLICATION-BASED: Scenario-based questions testing analytical ability

                CRITICAL RULES:
                - All 4 options must be PLASUABLE — avoid obviously wrong distractors
                - At least 2 options should seem potentially correct to an unprepared student
                - Include "trick" elements: close dates, similar-sounding schemes, confusing committees
                - Mix factual, conceptual, and analytical questions
                - Avoid: "All of the above", "None of the above" patterns (UPSC doesn't use these)
                - Use UPSC language: "Consider the following statements", "With reference to", "Which of the following is/are correct"

                Format each question EXACTLY as follows:
                ```json
                [
                  {
                    "question": "Consider the following statements:\\n1. [Statement]\\n2. [Statement]\\nWhich of the statements given above is/are correct?",
                    "options": {
                      "A": "(a) 1 only",
                      "B": "(b) 2 only",
                      "C": "(c) Both 1 and 2",
                      "D": "(d) Neither 1 nor 2"
                    },
                    "correctAnswer": "C",
                    "explanation": "Detailed explanation with facts, data, and constitutional/legal references. Explain why the correct answer is right AND why each wrong option is misleading.",
                    "upscRelevance": "This topic was asked in UPSC Prelims 20XX / frequently tested concept. Appears in GS Paper [X] / Current Affairs link."
                  }
                ]
                ```

                DISTRIBUTION GUIDELINES:
                - 30%% statement-based (2-3 statements)
                - 20%% pair matching (2023 pattern)
                - 20%% direct conceptual
                - 20%% application-based
                - 10%% assertion-reasoning

                ENSURE EACH QUESTION:
                - Tests understanding, not just recall
                - Has a clear single correct answer
                - Includes UPSC-relevant explanation with data/facts
                - References actual UPSC PYQ if applicable (year and paper)
                """.formatted(numberOfQuestions, subject, topic, difficulty);
    }

    // ============================================
    // ANSWER EVALUATION PROMPT
    // ============================================

    /**
     * Prompt for evaluating UPSC Mains answer writing
     */
    public static String answerEvaluationPrompt(String question, String userAnswer,
                                                String subject, int wordLimit) {
        return """
                You are a strict UPSC Mains answer evaluator. Evaluate the following answer using actual UPSC marking criteria.

                Subject: %s
                Question: %s
                Word Limit: %d words
                Marks: %s (10 marks if ~150 words, 15 marks if ~250 words)

                UPSC MARKING RUBRIC:
                Content (40%%): Accuracy of facts, depth of analysis, relevance to question, use of data/statistics/committee reports
                Structure (25%%): Introduction-Body-Conclusion format, headings/sub-headings, flow of arguments, paragraphing
                Analytical Ability (20%%): Critical thinking, multiple perspectives, balanced viewpoint, interlinking concepts
                Presentation (15%%): Clarity of expression, appropriate use of technical terms, diagrams/flowcharts mentioned, handwriting quality (if applicable)

                SPECIFIC CHECKS:
                ✓ Does the introduction define key terms or provide relevant context/data?
                ✓ Is the body multi-dimensional? (Social, Economic, Political, Ethical, Environmental, Legal angles)
                ✓ Are Constitutional Articles, Acts, Schemes, Committee Reports referenced?
                ✓ Is the conclusion forward-looking and solution-oriented?
                ✓ Does the answer stay within the word limit (±10%% tolerance)?
                ✓ Are there factual errors or outdated information?
                ✓ Does the answer directly address the question's demand (analyze, discuss, examine, critically evaluate)?

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
                  "presentationScore": 7,
                  "strengths": "What the student did well — be specific about dimensions covered, data used, structure followed",
                  "weaknesses": "What needs improvement — missing dimensions, factual errors, structural issues, weak intro/conclusion",
                  "suggestions": "Specific actionable suggestions — what to add, what to remove, how to restructure, which data/schemes to include",
                  "modelAnswer": "A model answer for this question within the %d-word limit, following UPSC Mains standards: intro-body-conclusion with multi-dimensional analysis",
                  "dimensionsMissed": "Important perspectives, dimensions, data points, schemes, or constitutional provisions the student missed",
                  "wordLimitStatus": "Within limit / Slightly over / Significantly over / Under",
                  "introQuality": "Good / Adequate / Poor — comment on whether it defines key terms, provides context/data",
                  "conclusionQuality": "Good / Adequate / Poor — comment on whether it's forward-looking and solution-oriented"
                }
                ```
                """.formatted(subject, question, wordLimit,
                wordLimit <= 150 ? "10" : "15",
                userAnswer,
                wordLimit);
    }

    // ============================================
    // STUDY PLAN PROMPT
    // ============================================

    /**
     * Prompt for generating a personalized UPSC study plan
     */
    public static String studyPlanPrompt(String name, String optionalSubject,
                                         String difficulty, int targetYear,
                                         int attemptNumber, int dailyHours,
                                         String weakSubjects, String strongSubjects) {
        return """
                Create a comprehensive, realistic, and actionable UPSC Civil Services study plan.

                Student Profile:
                - Name: %s
                - Optional Subject: %s
                - Current Level: %s (Beginner = start from NCERT, Intermediate = standard books, Advanced = revision/test series)
                - Target Year: %d (calculate months remaining from now)
                - Attempt Number: %d (1st attempt = build strong foundation, 2nd+ = focus on revision and test series)
                - Available Study Hours/Day: %d hours
                - Weak Subjects: %s
                - Strong Subjects: %s

                Create a COMPREHENSIVE plan with these components:

                1. PHASE-WISE ROADMAP:
                   - Phase 1: Foundation (NCERT Class 6-12 across all subjects) — [X weeks]
                   - Phase 2: Standard Books (subject-wise deep dive) — [X weeks]
                   - Phase 3: Optional Subject Mastery (Paper I + Paper II) — [X weeks]
                   - Phase 4: Answer Writing Practice + Test Series — [X weeks]
                   - Phase 5: Revision + Current Affairs Integration — [X weeks]
                   - Phase 6: Prelims-Specific Preparation (last 3 months before Prelims)

                2. DAILY TIMETABLE (based on %d hours):
                   - Morning block (3 hours): New/difficult subjects (fresh mind)
                   - Afternoon block (2 hours): Current affairs + newspaper reading + making notes
                   - Evening block (2 hours): Answer writing practice + revision
                   - Night block (1 hour): Optional subject + light reading

                3. SUBJECT-WISE STRATEGY:
                   For each GS subject: Standard book reference, NCERT chapters to cover first,
                   time allocation per week, number of revisions needed, PYQ integration

                4. OPTIONAL SUBJECT PLAN:
                   Paper I syllabus coverage timeline, Paper II syllabus coverage timeline,
                   previous year question analysis, answer writing practice schedule

                5. WEEKLY REVISION CYCLE:
                   - Monday-Saturday: New content + answer writing
                   - Sunday: Revision of entire week's topics + 1 mock test
                   - Spaced repetition: 1-day, 3-day, 7-day, 30-day revision schedule

                6. ANSWER WRITING SCHEDULE:
                   - Start from [when]: 2 answers/day (Mains preparation phase)
                   - Increase to [when]: 4 answers/day (intensive phase)
                   - Weekly: 1 essay practice (alternate weeks)
                   - Include GS1, GS2, GS3, GS4, and Optional subject answers

                7. CURRENT AFFAIRS STRATEGY:
                   - Daily: The Hindu/Indian Express (1.5 hours)
                   - Monthly: Yojana + Kurukshetra + Vision IAS Monthly Current Affairs
                   - Integration: Link each current event to static syllabus topics
                   - Maintain a running notebook: Topic → Current event link → Potential question

                8. TEST SERIES INTEGRATION:
                   - Prelims mock tests: Start [X months] before exam, 2/week initially → daily in last month
                   - Mains mock tests: Start [X months] before Mains, 1 full test/week → 2/week
                   - Analysis: After each test, note mistakes and weak areas for targeted revision

                9. WEAK SUBJECT IMPROVEMENT:
                   Specific action plan for: %s
                   - Extra time allocation
                   - Different approach/resources
                   - Targeted PYQ practice

                10. MILESTONES & CHECKPOINTS:
                    - Monthly: Assess coverage percentage, take a full-length topic test
                    - Quarterly: Full revision cycle, self-assessment test
                    - Buffer days: 4 days per month for catch-up

                IMPORTANT CONSIDERATIONS:
                - %d hours/day means [X hours/week, Y hours/month] — be realistic about coverage
                - Account for %d attempt — first attempters need stronger foundation; repeaters need revision + test series
                - Include buffer days (20%% of plan) for flexibility — UPSC preparation is unpredictable
                - Balance static subjects with current affairs (70:30 ratio)
                - Health: 7 hours sleep, 30 min exercise, 1 hour recreation — non-negotiable
                - Exam countdown: Calculate days remaining and work backwards from exam date
                """.formatted(name, optionalSubject, difficulty, targetYear,
                attemptNumber, dailyHours,
                weakSubjects, strongSubjects,
                dailyHours,
                weakSubjects,
                dailyHours,
                attemptNumber);
    }

    // ============================================
    // PYQ ANALYSIS PROMPT
    // ============================================

    public static String pyqAnalysisPrompt(String subject, String topic) {
        return """
                Provide a comprehensive Previous Year Question (PYQ) analysis for UPSC Civil Services on:

                Subject: %s
                Topic: %s

                Include ALL of the following:

                1. PYQ TREND ANALYSIS (2013-2024):
                   - How frequently this topic appears in Prelims (count per year)
                   - How frequently this topic appears in Mains (which GS paper, count per year)
                   - Increasing/decreasing/stable trend
                   - Difficulty progression over years

                2. QUESTION PATTERNS:
                   - Factual questions (direct recall) — what percentage
                   - Conceptual questions (understanding required) — what percentage
                   - Application-based questions (analytical thinking) — what percentage
                   - Current affairs-linked questions — what percentage

                3. ACTUAL PYQs (List 8-10 representative questions):
                   Format: [Year] [Prelims/Mains] [GS Paper] — Question text
                   Example: 2023 Prelims GS1 — Consider the following statements about...

                4. MODEL ANSWERS for Mains questions (150-250 words each):
                   Follow UPSC structure: Introduction → Multi-dimensional Body → Conclusion

                5. KEY TAKEAWAYS:
                   - What UPSC specifically tests on this topic (not what is commonly taught)
                   - The depth level required (NCERT vs. standard book vs. advanced)
                   - Common traps and confusing aspects

                6. CONNECTED TOPICS:
                   - Topics often asked together (UPSC combines concepts)
                   - Cross-paper linkages (e.g., Polity topic appearing in GS2 Governance)

                7. PREDICTED QUESTIONS (3-5 potential questions for upcoming exam):
                   Based on current affairs trends, recent developments, and pattern analysis

                8. REVISION STRATEGY:
                   - What to prioritize for Prelims (factual, data-heavy aspects)
                   - What to prioritize for Mains (analytical, multi-dimensional aspects)
                   - How to make revision notes for this topic

                Note: Frame questions in actual UPSC style and difficulty. Be specific about years, papers, and mark patterns.
                """.formatted(subject, topic);
    }

    // ============================================
    // CURRENT AFFAIRS ANALYSIS PROMPT
    // ============================================

    public static String currentAffairsPrompt(String topic) {
        return """
                As a UPSC mentor, provide a comprehensive analysis of the following current affairs topic:

                Topic: %s

                Cover ALL of these dimensions:

                1. WHAT: Brief factual summary — what happened, when, where, who is involved
                2. WHY IMPORTANT: Why UPSC aspirants should care — syllabus link, exam relevance
                3. BACKGROUND: Historical context, evolution, previous developments leading to this
                4. STATIC SYLLABUS LINK: Which GS paper (GS1/GS2/GS3/GS4), which specific topic in the UPSC syllabus
                5. MULTI-DIMENSIONAL ANALYSIS:
                   - Political/Governance dimension
                   - Economic dimension (data, budget allocation, GDP impact)
                   - Social dimension (impact on people, marginalized sections)
                   - Legal/Constitutional dimension (Articles, Acts, court judgments)
                   - Environmental dimension (if applicable)
                   - International dimension (global implications, India's stance)
                6. GOVERNMENT RESPONSE:
                   - Relevant schemes and policies
                   - Constitutional provisions involved
                   - Ministry/department handling the issue
                   - Budget allocations and fund utilization
                7. EXPERT PERSPECTIVES: Different viewpoints — government, opposition, civil society, international bodies
                8. PREVIOUS YEAR LINKS: Was this topic (or related) asked in any UPSC PYQ (2013-2024)?
                9. POTENTIAL QUESTIONS:
                   - Prelims: 2 MCQ-style questions UPSC might ask
                   - Mains: 1 descriptive question (150 words) UPSC might ask
                10. KEYWORDS FOR ANSWER WRITING: Important terms, data points, and phrases to use in Mains answers
                11. RELATED CURRENT EVENTS: Other news items connected to this topic
                12. FURTHER READING: What to study next — NCERT chapter, standard book chapter, government report

                Note: Mention which GS paper this relates to. Include specific data, percentages, and report findings where relevant.
                Acknowledge your knowledge cutoff and advise students to check latest updates from PIB and newspapers.
                """.formatted(topic);
    }
}
