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
import java.util.*;

@RestController
public class FacadeController {
    private final WebClient messageClient;
    private final List<WebClient> loggingClients;

    Logger logger = LoggerFactory.getLogger(FacadeController.class);

    public FacadeController() {
        loggingClients = List.of(
                WebClient.create("http://localhost:8084"),
                WebClient.create("http://localhost:8085"),
                WebClient.create("http://localhost:8086"));
        messageClient = WebClient.create("http://localhost:8083");
    }

    @GetMapping("/facade-service")
    public String getFacadeService() {
        List<WebClient> shuffledClients = new ArrayList<>(loggingClients);
        Collections.shuffle(shuffledClients);
        String loggingResponse = null;
        for (WebClient loggingClient : shuffledClients) {
            try {
                loggingResponse = loggingClient.get()
                        .uri("/login-history")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                if (loggingResponse != null) {
                    break;
                }
            } catch (Exception ex) {
                logger.info("Client {} failed: {}", loggingClient, ex.getMessage());
            }
        }
        if (loggingResponse == null) {
            throw new RuntimeException("All logging clients failed.");
        }

        var messageResponse = messageClient.get()
                .uri("/message")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return messageResponse + ":\n" + loggingResponse;
    }

    @PostMapping("/facade-service")
    public String postFacadeService(@RequestBody String text) {
        var msg = new Message(UUID.randomUUID(), text);
        List<WebClient> shuffledClients = new ArrayList<>(loggingClients);
        Collections.shuffle(shuffledClients);
        for (WebClient loggingClient : shuffledClients) {
            try {
                return loggingClient.post()
                        .uri("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(msg), Message.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception ex) {
                logger.info("Client {} failed: {}", loggingClient, ex.getMessage());
            }
        }
        throw new RuntimeException("All logging clients failed.");
    }


    private WebClient getRandomLoggingClient() {
        Random random = new Random();
        int index = random.nextInt(loggingClients.size());
        return loggingClients.get(index);
    }
}
