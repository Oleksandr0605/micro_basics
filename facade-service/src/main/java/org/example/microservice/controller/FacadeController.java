package org.example.microservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.catalog.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    Logger logger = LoggerFactory.getLogger(FacadeController.class);
    private final HazelcastInstance hazelcastInstance;
    private final Consul consul;

    @Autowired
    public FacadeController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.consul = Consul.builder().build();
    }

    @GetMapping("/facade-service")
    public ResponseEntity<String> getFacadeService() {
        List<CatalogService> loggingServices = consul.catalogClient().getService("logging-server").getResponse();
        if (loggingServices == null || loggingServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No logging service available");
        }
        Random random = new Random();
        int index = random.nextInt(loggingServices.size());

        String loggingResponse = null;
        for (int i = 0; i < loggingServices.size(); ++i) {
            CatalogService catalogService = loggingServices.get((index + i) % loggingServices.size());
            String baseUrl = "http://" + catalogService.getAddress() + ":" + catalogService.getServicePort();
            WebClient loggingClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .build();
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

        List<CatalogService> messageServices = consul.catalogClient().getService("message-server").getResponse();
        if (messageServices == null || messageServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No message service available");
        }
        index = random.nextInt(messageServices.size());

        Map<UUID, String> messageResponse = null;
        for (int i = 0; i < messageServices.size(); ++i) {
            CatalogService catalogService = messageServices.get((index + i) % messageServices.size());
            String baseUrl = "http://" + catalogService.getAddress() + ":" + catalogService.getServicePort();
            WebClient messageClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .build();
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
        return ResponseEntity.ok(messageResponse + ":\n" + loggingResponse);
    }

    @PostMapping("/facade-service")
    public ResponseEntity<String> postFacadeService(@RequestBody String text) {
        List<CatalogService> loggingServices = consul.catalogClient().getService("logging-server").getResponse();
        if (loggingServices == null || loggingServices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No logging service available");
        }
        Random random = new Random();
        int index = random.nextInt(loggingServices.size());

        var msg = new Message(UUID.randomUUID(), text);
        String serializedMsg;
        serializedMsg = msg.toString();
        IQueue<String> messageQueue = hazelcastInstance.getQueue("messageQueue");
        messageQueue.add(serializedMsg);
        for (int i = 0; i < loggingServices.size(); ++i) {
            CatalogService catalogService = loggingServices.get((index + i) % loggingServices.size());
            String baseUrl = "http://" + catalogService.getAddress() + ":" + catalogService.getServicePort();
            WebClient loggingClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .build();
            try {
                return loggingClient.post()
                        .uri("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(msg), Message.class)
                        .retrieve()
                        .toEntity(String.class)
                        .block();
            } catch (Exception ex) {
                logger.info("Client {} failed: {}", loggingClient, ex.getMessage());
            }
        }
        throw new RuntimeException("All logging clients failed.");
    }
}
