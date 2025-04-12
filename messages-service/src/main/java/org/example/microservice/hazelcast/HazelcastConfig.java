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
        queueConfig.setBackupCount(2);
        queueConfig.setAsyncBackupCount(0);
        config.addQueueConfig(queueConfig);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getTcpIpConfig()
                .setEnabled(true)
                .addMember("127.0.0.1");
        return Hazelcast.newHazelcastInstance(config);
    }
}