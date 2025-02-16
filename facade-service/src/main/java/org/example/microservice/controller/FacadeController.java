package org.example.microservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.example.microservice.message.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.UUID;

@RestController
public class FacadeController {
    WebClient loggingClient = WebClient.create("http://localhost:8082");
    WebClient messageClient = WebClient.create("http://localhost:8083");

    Logger logger = LoggerFactory.getLogger(FacadeController.class);

    @GetMapping("/facade-service")
    public String getFacadeService() {
        var loggingResponse = loggingClient.get().uri("/login-history").retrieve().bodyToMono(String.class).block();
        var messageResponse = messageClient.get().uri("/message").retrieve().bodyToMono(String.class).block();
        return messageResponse + ":\n" + loggingResponse;
    }

    @PostMapping("/facade-service")
    public String postFacadeService(@RequestBody String text) {
        var msg = new Message(UUID.randomUUID(), text);
        return loggingClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(msg), Message.class)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .doBeforeRetry(retrySignal -> logger.info("Retry attempt: {}, Reason: {}",
                                        retrySignal.totalRetries(), retrySignal.failure().getMessage())))
                .block();
    }
}
