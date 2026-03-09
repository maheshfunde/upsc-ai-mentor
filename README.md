# UPSC AI Mentor

AI-powered UPSC preparation platform built with Spring Boot + Thymeleaf + LangChain4j.

It provides guided learning modules for chat tutoring, quiz practice, answer evaluation, study planning, current affairs analysis, and PYQ analysis.

## Features

- AI Tutor Chat with subject-wise guidance
- Persistent chat history with old chat session selector
- Quiz generation + performance tracking
- Mains answer writing evaluation with AI feedback
- Personalized study plan and progress analysis
- Current Affairs Analysis page (paste article/news only)
- PYQ Analysis page (topic-based PYQ + model answers)
- AI Settings page:
  - Use local Ollama by default
  - Optionally configure online API key/model (OpenAI or Groq)
  - Auto fallback to local model if online call fails
- Collapsible sidebar navigation

## Tech Stack

- Java 21
- Spring Boot 4
- Spring MVC + Thymeleaf
- Spring Data JPA + H2
- LangChain4j (`ollama`, `open-ai`)
- Vanilla JS + CSS

## Project Structure

```text
src/main/java/com/upscmentor
  config/
  controller/
  model/
    dto/
    entity/
    enums/
  prompts/
  repository/
  service/

src/main/resources
  templates/
  static/css
  static/js
  application.yaml
```

## Prerequisites

- JDK 21
- Maven (or use `mvnw`)
- Optional for local LLM: Ollama running at `http://localhost:11434`

## Run Locally

```bash
# from project root
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Open app: `http://localhost:8080`

## AI Model Modes

Default behavior:
- If no online key is configured: uses local Ollama model
- If online key is configured: uses online model
- If online request fails: falls back to local model

Online key notes:
- OpenAI keys typically start with `sk-`
- Groq keys start with `gsk_` (auto-routed to Groq OpenAI-compatible base URL)

Configure this in UI:
- `AI Settings` page (`/ai-settings`)

## Main Pages

- `/dashboard`
- `/chat`
- `/quiz`
- `/answer-practice`
- `/study-plan`
- `/current-affairs`
- `/pyq-analysis`
- `/ai-settings`

## API Overview

### User
- `POST /api/user/register`
- `GET /api/user/{id}`
- `GET /api/user/by-username/{username}`
- `POST /api/user/{id}/llm-config`
- `GET /api/user/{id}/llm-config`
- `DELETE /api/user/{id}/llm-config`

### Chat
- `POST /api/chat/send`
- `POST /api/chat/new-session`
- `GET /api/chat/session/{sessionId}?userId={id}`
- `GET /api/chat/sessions?userId={id}`

### Quiz
- `POST /api/quiz/generate`
- `POST /api/quiz/submit`
- `GET /api/quiz/history/{userId}`
- `GET /api/quiz/performance/{userId}`

### Answer Practice
- `POST /api/answer/evaluate`
- `GET /api/answer/generate-question`

### Study Plan / Progress
- `GET /api/study-plan/generate/{userId}`
- `GET /api/study-plan/daily/{userId}`
- `GET /api/study-plan/weekly/{userId}`
- `GET /api/study-plan/progress/{userId}`
- `GET /api/study-plan/analysis/{userId}`

### Content Analysis
- `POST /api/content/current-affairs`
- `POST /api/content/pyq-analysis`

## Configuration

Application config file:
- `src/main/resources/application.yaml`

Important sections:
- `spring.datasource` (H2 setup)
- `ai.ollama.*`
- `ai.online.*`
