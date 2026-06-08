package com.upscmentor.service;

import com.upscmentor.repository.ChatHistoryRepository;
import com.upscmentor.repository.ChatSessionMetaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
}
