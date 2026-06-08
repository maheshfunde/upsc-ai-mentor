package com.upscmentor.service;

import com.upscmentor.model.dto.UserProfileRequest;
import com.upscmentor.model.entity.User;
import com.upscmentor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Create a new user profile during onboarding
     */
    public User createUser(UserProfileRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setOptionalSubject(request.getOptionalSubject());
        user.setDifficultyLevel(request.getDifficultyLevel());
        user.setTargetYear(request.getTargetYear());
        user.setAttemptNumber(request.getAttemptNumber());
        user.setDailyStudyHours(request.getDailyStudyHours());
        user.setWeakSubjects(request.getWeakSubjects());
        user.setStrongSubjects(request.getStrongSubjects());

        User savedUser = userRepository.save(user);
        logger.info("New user created: {} with optional subject: {}",
                savedUser.getUsername(), savedUser.getOptionalSubject().getDisplayName());

        return savedUser;
    }

    /**
     * Get user by ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }

    /**
     * Get user by username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Update user profile
     */
    public User updateUser(Long id, UserProfileRequest request) {
        User user = getUserById(id);
        user.setName(request.getName());
        user.setOptionalSubject(request.getOptionalSubject());
        user.setDifficultyLevel(request.getDifficultyLevel());
        user.setTargetYear(request.getTargetYear());
        user.setAttemptNumber(request.getAttemptNumber());
        user.setDailyStudyHours(request.getDailyStudyHours());
        user.setWeakSubjects(request.getWeakSubjects());
        user.setStrongSubjects(request.getStrongSubjects());
        user.setLastActive(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update last active timestamp
     */
    public void updateLastActive(Long userId) {
        User user = getUserById(userId);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Save or update online LLM configuration for a user
     */
    public void updateOnlineLlmConfig(Long userId, String apiKey, String modelName, String baseUrl) {
        User user = getUserById(userId);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API key is required");
        }
        user.setOpenAiApiKey(apiKey.trim());
        user.setOnlineModelName(
                modelName != null && !modelName.trim().isEmpty() ? modelName.trim() : null
        );
        user.setOnlineBaseUrl(
                baseUrl != null && !baseUrl.trim().isEmpty() ? baseUrl.trim() : null
        );
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Save local LLM preference and disable online mode for a user
     */
    public void updateLocalLlmConfig(Long userId, String localModelName) {
        User user = getUserById(userId);
        if (localModelName == null || localModelName.trim().isEmpty()) {
            throw new RuntimeException("Local model name is required");
        }
        user.setLocalModelName(localModelName.trim());
        user.setOpenAiApiKey(null);
        user.setOnlineModelName(null);
        user.setOnlineBaseUrl(null);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Clear online LLM configuration for a user
     */
    public void clearLlmConfig(Long userId) {
        User user = getUserById(userId);
        user.setOpenAiApiKey(null);
        user.setOnlineModelName(null);
        user.setOnlineBaseUrl(null);
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }
}
