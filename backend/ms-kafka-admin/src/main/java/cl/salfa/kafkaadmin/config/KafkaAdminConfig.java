package cl.salfa.kafkaadmin.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaAdminConfig {

    @Bean(destroyMethod = "close")
    public AdminClient adminClient(
            @Value("${spring.kafka.bootstrap-servers}")
            String bootstrapServers
    ) {

        Map<String, Object> config = new HashMap<>();

        config.put(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        config.put(
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG,
                5000
        );

        config.put(
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG,
                10000
        );

        return AdminClient.create(config);
    }
}