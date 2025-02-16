package org.example.microservice.controller;

import org.example.microservice.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LoggingController {

    Logger logger = LoggerFactory.getLogger(LoggingController.class);
    private final Map<UUID, String> messages = new ConcurrentHashMap<>();

    @GetMapping("/login-history")
    public String logHistory() {
        return messages.values().toString();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody Message msg) {
        if (messages.containsKey(msg.getId())) {
            logger.info("Got duplicate with id: {}", msg.getId());
            return ResponseEntity.ok().build();
        }

        logger.info(msg.getText());
        messages.put(msg.getId(), msg.getText());
        return ResponseEntity.ok().build();
    }
}
