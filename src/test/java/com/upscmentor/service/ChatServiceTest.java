package com.upscmentor.service;

import com.upscmentor.model.dto.ChatRequest;
import com.upscmentor.model.dto.ChatResponse;
import com.upscmentor.model.entity.ChatHistory;
import com.upscmentor.repository.ChatHistoryRepository;
import com.upscmentor.repository.ChatSessionMetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ChatServiceTest {

    @MockBean
    private AiModelRouterService aiModelRouterService;

    @MockBean
    private ChatHistoryRepository chatHistoryRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private PromptService promptService;

    @MockBean
    private ChatSessionMetaRepository chatSessionMetaRepository;

    @Test
    public void contextLoads() {
        // Spring Boot test context loads successfully with all mocked dependencies
    }

    @Test
    void shouldReturnErrorResponseOnTimeout() {
        when(aiModelRouterService.generate(any(), any(), any()))
            .thenThrow(new RuntimeException("Request timed out"));

        ChatRequest request = new ChatRequest();
        request.setMessage("Test question");
        request.setSessionId("test-session");
        request.setUserId(1L);

        ChatResponse response = chatService.chat(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("took too long"));
    }

    @Test
    void shouldReturnErrorResponseOnConnectionFailure() {
        when(aiModelRouterService.generate(any(), any(), any()))
            .thenThrow(new RuntimeException("Connection refused"));

        ChatRequest request = new ChatRequest();
        request.setMessage("Test question");
        request.setSessionId("test-session");
        request.setUserId(1L);

        ChatResponse response = chatService.chat(request);

        assertFalse(response.isSuccess());
        assertTrue(response.getError().contains("Cannot connect"));
    }

    @Test
    void shouldCreateNewSessionWhenNoneProvided() {
        when(chatHistoryRepository.findBySessionIdOrderByCreatedAtAsc(any()))
            .thenReturn(Collections.emptyList());
        when(aiModelRouterService.generate(any(), any(), any()))
            .thenReturn("Test response");

        ChatRequest request = new ChatRequest();
        request.setMessage("Hello");
        request.setUserId(1L);

        ChatResponse response = chatService.chat(request);

        assertTrue(response.isSuccess());
        assertNotNull(response.getSessionId());
    }
}
