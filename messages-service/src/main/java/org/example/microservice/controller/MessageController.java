package org.example.microservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.collection.IQueue;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import org.example.microservice.message.Message;
import com.hazelcast.core.HazelcastInstance;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MessageController {
    private final Map<UUID, String> messageStore = new ConcurrentHashMap<>();
    private final Consul consul;

    private final HazelcastInstance hazelcastInstance;

    public MessageController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.consul = Consul.builder().build();
    }

    @GetMapping("/message")
    public Map<UUID, String> getMessages() {
        return messageStore;
    }

    @PostConstruct
    public void startMessagePollingThread() {
        new Thread(() -> {
            KeyValueClient kvClient = consul.keyValueClient();
            String queueName = kvClient.getValueAsString("message_queue_name")
                    .orElse("defaultQueue");
            IQueue<String> messageQueue = hazelcastInstance.getQueue(queueName);
            while (true) {
                String msg_str = messageQueue.poll();
                if (msg_str != null) {
                    Message msg = new Message(msg_str);
                    System.out.println(msg);
                    messageStore.put(msg.getId(), msg.getText());
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "MessagePollingThread").start();
    }
}
