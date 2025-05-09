package org.example.microservice.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        Consul consul = Consul.builder().build();
        KeyValueClient kvClient = consul.keyValueClient();
        String queueName = kvClient.getValueAsString("message_queue_name")
                .orElse("defaultQueue");
        int backupCount = Integer.parseInt(kvClient.getValueAsString("queue/backup_count")
                .orElse("defaultQueue"));
        QueueConfig queueConfig = new QueueConfig(queueName);
        queueConfig.setBackupCount(backupCount);
        config.addQueueConfig(queueConfig);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig()
                .setEnabled(true)
                .addMember("127.0.0.1");
        return Hazelcast.newHazelcastInstance(config);
    }
}