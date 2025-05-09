package org.example.microservice.controller;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import org.example.microservice.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RestController
public class LoggingController {

    Logger logger = LoggerFactory.getLogger(LoggingController.class);

    private final HazelcastInstance hazelcastInstance;
    private final IMap<UUID, String> loggingMap;
    private final Consul consul;

    @Autowired
    public LoggingController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
        this.consul = Consul.builder().build();
        KeyValueClient kvClient = consul.keyValueClient();
        String loggingMapName = kvClient.getValueAsString("logging_map_name")
                .orElse("defaultQueue");
        this.loggingMap = hazelcastInstance.getMap(loggingMapName);
    }

    @GetMapping("/login-history")
    public ResponseEntity<String> logHistory() {
        logger.info(loggingMap.values().toString());
        loggingMap.flush();
        return ResponseEntity.ok(loggingMap.values().toString());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Message msg) {
        String address = hazelcastInstance.getCluster().getLocalMember().getAddress().toString();
        logger.info("Message: {} from: {}", msg.getText(), address);
        if (loggingMap.containsKey(msg.getId())) {
            logger.info("Got duplicate with id: {}", msg.getId());
            return ResponseEntity.ok("Duplicate login");
        }
        logger.info(msg.getText());
        loggingMap.put(msg.getId(), msg.getText());
        return ResponseEntity.ok("Login successful");
    }
}
