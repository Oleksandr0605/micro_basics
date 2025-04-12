package org.example.microservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
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
    private final List<WebClient> messageClients;
    private final List<WebClient> loggingClients;

    Logger logger = LoggerFactory.getLogger(FacadeController.class);
    private final HazelcastInstance hazelcastInstance;
    private final ObjectMapper objectMapper;

    public FacadeController(HazelcastInstance hazelcastInstance) {
        loggingClients = List.of(
                WebClient.create("http://localhost:8084"),
                WebClient.create("http://localhost:8085"),
                WebClient.create("http://localhost:8086"));
        messageClients = List.of(
                WebClient.create("http://localhost:8083"),
                WebClient.create("http://localhost:8082")
                );
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = new ObjectMapper();
    }

    @GetMapping("/facade-service")
    public String getFacadeService() {
        List<WebClient> shuffledClients = new ArrayList<>(loggingClients);
        List<WebClient> shuffledMessageClients = new ArrayList<>(messageClients);
        Collections.shuffle(shuffledClients);
        Collections.shuffle(shuffledMessageClients);
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

        Map<UUID, String> messageResponse = null;
        for (WebClient messageClient : shuffledMessageClients) {
            try {
                messageResponse = messageClient.get()
                        .uri("/message")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<UUID, String>>() {})
                        .block();
                if (messageResponse != null) {
                    break;
                }
            } catch (Exception ex) {
                logger.info("Client {} failed: {}", messageClient, ex.getMessage());
            }
        }
        if (messageResponse == null) {
            messageResponse = new HashMap<>();
        }
        return messageResponse + ":\n" + loggingResponse;
    }

    @PostMapping("/facade-service")
    public String postFacadeService(@RequestBody String text) {
        var msg = new Message(UUID.randomUUID(), text);
        String serializedMsg;
        serializedMsg = msg.toString();
        IQueue<String> messageQueue = hazelcastInstance.getQueue("messageQueue");
        messageQueue.add(serializedMsg);
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
