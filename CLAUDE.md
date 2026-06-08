# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**UPSC AI Mentor** — AI-powered UPSC Civil Services preparation platform. Provides chat tutoring, quiz practice, answer evaluation, study planning, current affairs analysis, and PYQ (Previous Year Questions) analysis.

## Tech Stack

- **Java 21**, **Spring Boot 4** (Spring MVC + Thymeleaf)
- **Spring Data JPA** with **H2** file-based database (`./data/upscmentor`)
- **LangChain4j 0.31.0** (`langchain4j-ollama`, `langchain4j-open-ai`)
- **Lombok** for boilerplate reduction
- Vanilla JS + CSS for frontend

## Architecture

### Layered Structure (`com.upscmentor`)

```
config/         → Spring config (AiConfig for Ollama bean, WebConfig for CORS)
controller/     → REST endpoints + page routing (6 controllers)
service/        → Business logic (9 services)
repository/     → Spring Data JPA interfaces (6 repositories)
model/
  entity/       → JPA entities (User, ChatHistory, QuizResult, StudyPlan, ProgressRecord)
  dto/          → Request/Response DTOs
  enums/        → Domain enums (Subject, OptionalSubject, ExamStage, DifficultyLevel)
prompts/        → AI system prompt templates (SystemPrompts, SubjectPrompts)
```

### AI Model Routing

`AiModelRouterService` is the core AI gateway:
- Default: local Ollama model (`llama3:8b` at `http://localhost:11434`)
- If user configures an API key: routes to OpenAI or Groq (auto-detected by key prefix `sk-` vs `gsk_`)
- Auto-fallback to local Ollama if online request fails
- Each service (ChatService, QuizService, etc.) calls `aiModelRouterService.generate(user, prompt)` rather than calling a model directly

### Key Flow Pattern

Controllers receive requests → delegate to services → services use `AiModelRouterService` for AI calls → results saved via repositories → responses returned to Thymeleaf templates or as JSON.

### Pages & Routes

| Page | Template | Controller |
|------|----------|------------|
| `/dashboard` | `index.html` | UserController |
| `/chat` | `chat.html` | ChatController |
| `/quiz` | `quiz.html` | QuizController |
| `/answer-practice` | `answer-practice.html` | AnswerPracticeController |
| `/study-plan` | `study-plan.html` | StudyPlanController |
| `/current-affairs` | (via ContentAnalysisController) | ContentAnalysisController |
| `/pyq-analysis` | `pyq-analysis.html` | ContentAnalysisController |
| `/ai-settings` | (via UserController) | UserController |

### Database

H2 file-based at `./data/upscmentor`. Console available at `/h2-console`. DDL auto-update enabled. Seeded data in `src/main/resources/data.sql`.

## Development Commands

All commands run from project root:

```bash
# Run the application (Windows PowerShell)
.\mvnw spring-boot:run

# Run all tests
.\mvnw test

# Run a single test class
.\mvnw test -Dtest=ChatServiceTest

# Run a single test method
.\mvnw test -Dtest=ChatServiceTest#testChat

# Build (skip tests)
.\mvnw package -DskipTests

# Build and run JAR
.\mvnw package && java -jar target/upsc-ai-mentor-0.0.1-SNAPSHOT.jar

# Clean build
.\mvnw clean install
```

**App runs on:** `http://localhost:8080`
**H2 Console:** `http://localhost:8080/h2-console`

## Configuration

- `src/main/resources/application.yaml` — main config (datasource, AI, UPSC word limits)
- AI settings are also configurable per-user via the `/ai-settings` UI page (stored in User entity)
- `upsc.max-answer-words`, `upsc.mains-answer-words`, `upsc.essay-word-limit` control evaluation word limits

## Tests

Test classes in `src/test/java/com/upscmentor/service/`:
- `ChatServiceTest` — chat session management, AI routing
- `QuizServiceTest` — quiz generation and scoring
- `AnswerEvaluationServiceTest` — answer evaluation logic

Tests use `@SpringBootTest` with in-memory H2.

## Docker

`docker-compose.yml` available for containerized deployment.
