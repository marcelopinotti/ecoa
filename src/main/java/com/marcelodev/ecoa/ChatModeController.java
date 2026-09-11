package com.marcelodev.ecoa;

import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatModeController {

    private final GoogleGenAiChatModel chatModel;

    public ChatModeController(GoogleGenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping
    public String getChatMode(String prompt) {
        return this.chatModel.call(prompt);
    }
}
