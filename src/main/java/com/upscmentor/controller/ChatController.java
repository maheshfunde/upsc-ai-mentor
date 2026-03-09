package com.upscmentor.controller;

import com.upscmentor.model.dto.ChatRequest;
import com.upscmentor.model.dto.ChatResponse;
import com.upscmentor.model.enums.Subject;
import com.upscmentor.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /api/chat/send - Send message to AI mentor
     */
    @PostMapping("/send")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * POST /api/chat/new-session - Create new chat session
     */
    @PostMapping("/new-session")
    public ResponseEntity<Map<String, String>> newSession() {
        String sessionId = chatService.createNewSession();
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    /**
     * GET /api/chat/subjects - List all available subjects
     */
    @GetMapping("/subjects")
    public ResponseEntity<List<Map<String, String>>> getSubjects() {
        List<Map<String, String>> subjects = Arrays.stream(Subject.values())
                .map(s -> Map.of(
                        "value", s.name(),
                        "label", s.getDisplayName(),
                        "stage", s.getExamStage(),
                        "description", s.getDescription()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    /**
     * GET /api/chat/session/{sessionId}?userId=1 - Get persisted messages for a session
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getSessionHistory(@PathVariable String sessionId,
                                               @RequestParam Long userId) {
        List<Map<String, Object>> messages = chatService.getSessionHistory(userId, sessionId).stream()
                .map(m -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("role", m.getRole());
                    item.put("content", m.getContent());
                    item.put("createdAt", m.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/chat/sessions?userId=1 - List old chat sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<?> getOldSessions(@RequestParam Long userId) {
        return ResponseEntity.ok(chatService.getSessionSummaries(userId));
    }

    /**
     * GET /api/chat/health - Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "running",
                "application", "UPSC AI Mentor",
                "model", "ollama/llama3"
        ));
    }
}
