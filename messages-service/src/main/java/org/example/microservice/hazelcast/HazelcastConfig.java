package org.example.microservice.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {
    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        QueueConfig queueConfig = new QueueConfig("messageQueue");
        queueConfig.setBackupCount(1);
        config.addQueueConfig(queueConfig);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        return Hazelcast.newHazelcastInstance(config);
    }
}