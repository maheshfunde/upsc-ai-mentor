package com.upscmentor.controller;

import com.upscmentor.model.dto.UserProfileRequest;
import com.upscmentor.model.dto.LlmConfigRequest;
import com.upscmentor.model.entity.User;
import com.upscmentor.model.enums.DifficultyLevel;
import com.upscmentor.model.enums.OptionalSubject;
import com.upscmentor.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/user/register - Create new user profile
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserProfileRequest request) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", user.getId(),
                    "message", "Welcome " + user.getName() + "! Your UPSC preparation journey begins now!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/user/{id} - Get user profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/user/by-username/{username} - Get user profile by username
     */
    @GetMapping("/by-username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/user/{id} - Update user profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @Valid @RequestBody UserProfileRequest request) {
        try {
            User user = userService.updateUser(id, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Profile updated successfully!"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/user/optional-subjects - List all optional subjects
     */
    @GetMapping("/optional-subjects")
    public ResponseEntity<List<Map<String, String>>> getOptionalSubjects() {
        List<Map<String, String>> subjects = Arrays.stream(OptionalSubject.values())
                .map(s -> Map.of(
                        "value", s.name(),
                        "label", s.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    /**
     * GET /api/user/difficulty-levels - List all difficulty levels
     */
    @GetMapping("/difficulty-levels")
    public ResponseEntity<List<Map<String, String>>> getDifficultyLevels() {
        List<Map<String, String>> levels = Arrays.stream(DifficultyLevel.values())
                .map(d -> Map.of(
                        "value", d.name(),
                        "label", d.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(levels);
    }

    /**
     * GET /api/user/check-username/{username} - Check if username is available
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@PathVariable String username) {
        boolean available = !userService.usernameExists(username);
        return ResponseEntity.ok(Map.of("available", available));
    }

    /**
     * POST /api/user/{id}/llm-config - Save API key + online model preference
     */
    @PostMapping("/{id}/llm-config")
    public ResponseEntity<?> saveLlmConfig(@PathVariable Long id,
                                           @RequestBody LlmConfigRequest request) {
        try {
            if (request.getApiKey() == null || request.getApiKey().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "API key is required"
                ));
            }
            userService.updateLlmConfig(id, request.getApiKey(), request.getModelName());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Online model configuration saved"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/user/{id}/llm-config - Check whether online model is configured
     */
    @GetMapping("/{id}/llm-config")
    public ResponseEntity<?> getLlmConfigStatus(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            boolean configured = user.getOpenAiApiKey() != null && !user.getOpenAiApiKey().isBlank();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("onlineConfigured", configured);
            response.put("modelName", user.getOnlineModelName());
            response.put("provider", configured ? "OPENAI" : "OLLAMA");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/user/{id}/llm-config - Clear online model config and use local LLM
     */
    @DeleteMapping("/{id}/llm-config")
    public ResponseEntity<?> clearLlmConfig(@PathVariable Long id) {
        try {
            userService.clearLlmConfig(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Online model disabled. Falling back to local LLM."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}
