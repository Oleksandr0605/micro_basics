package org.example.microservice.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastCluster {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setInstanceName("hazelcast-instance");
        config.setClusterName("hazelcast-cluster");

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5742);
        networkConfig.setPortAutoIncrement(true);

        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        config.getJetConfig().setEnabled(true);
        joinConfig.getTcpIpConfig().setEnabled(true)
                .setConnectionTimeoutSeconds(10)
                .addMember("127.0.0.1");

        return Hazelcast.newHazelcastInstance(config);
    }
}
