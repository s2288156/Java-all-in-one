package org.all.ai.controller;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @Autowired
    private OllamaChatModel chatModel;


    @PostMapping("/test")
    public String test() {
        ChatResponse response = chatModel.call(
                new Prompt(
                        "Generate the names of 5 famous pirates.",
                        OllamaOptions.builder()
                                .model("llama3.1:8b")
                                .temperature(0.4)
                                .build()
                )
        );
        String text = response.getResult().getOutput().getText();
        log.info("response: {}", text);
        return text;
    }
}
