package cl.salfa.workshop.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
public class RabbitStartupConfig {

    private static final Logger log =
            LoggerFactory.getLogger(RabbitStartupConfig.class);

    private final ConnectionFactory connectionFactory;

    @Bean
    public ApplicationRunner initializeRabbitConnection() {

        return args -> {
            try (var connection = connectionFactory.createConnection()) {

                log.info(
                        "Conexión RabbitMQ establecida. Host: {}",
                        connection.getDelegate().getAddress()
                );

            } catch (Exception ex) {

                log.error(
                        "No fue posible establecer conexión con RabbitMQ",
                        ex
                );

                throw new IllegalStateException(
                        "RabbitMQ no está disponible durante el arranque",
                        ex
                );
            }
        };
    }
}