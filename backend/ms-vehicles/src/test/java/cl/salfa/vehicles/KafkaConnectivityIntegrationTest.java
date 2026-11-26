package cl.salfa.vehicles;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("external-kafka")
class KafkaConnectivityIntegrationTest {

    private static final String TOPIC = "vehicle.events";

    @Test
    void shouldPublishAndConsumeVehicleEvent() throws Exception {

        String bootstrapServers = System.getenv()
                .getOrDefault(
                        "KAFKA_BOOTSTRAP_SERVERS",
                        "localhost:9092"
                );

        String eventId = UUID.randomUUID().toString();
        String vin = "TESTVIN0000000001";

        String json = """
                {
                  "eventId": "%s",
                  "eventType": "VEHICLE_CREATED",
                  "eventVersion": 1,
                  "vehicleId": 1,
                  "vin": "%s",
                  "branch": "SANTIAGO",
                  "occurredAt": "%s"
                }
                """.formatted(eventId, vin, Instant.now());

        Properties producerProperties = new Properties();
        producerProperties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        producerProperties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        producerProperties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        producerProperties.put(
                ProducerConfig.ACKS_CONFIG,
                "all"
        );
        producerProperties.put(
                ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                true
        );

        Properties consumerProperties = new Properties();
        consumerProperties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        consumerProperties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "salfa360-java-test-" + UUID.randomUUID()
        );
        consumerProperties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        consumerProperties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        consumerProperties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );
        consumerProperties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );
        consumerProperties.put(
                ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG,
                false
        );

        try (
                KafkaProducer<String, String> producer =
                        new KafkaProducer<>(producerProperties);

                KafkaConsumer<String, String> consumer =
                        new KafkaConsumer<>(consumerProperties)
        ) {
            consumer.subscribe(List.of(TOPIC));

            long assignmentDeadline =
                    System.nanoTime() + Duration.ofSeconds(20).toNanos();

            while (consumer.assignment().isEmpty()
                    && System.nanoTime() < assignmentDeadline) {
                consumer.poll(Duration.ofMillis(250));
            }

            assertFalse(
                    consumer.assignment().isEmpty(),
                    "El consumidor no recibió particiones"
            );

            // Establecer la posición inicial antes de publicar.
            for (var partition : consumer.assignment()) {
                consumer.position(partition);
            }

            var metadata = producer.send(
                    new ProducerRecord<>(TOPIC, vin, json)
            ).get();

            System.out.printf(
                    "PUBLICADO: partition=%d, offset=%d%n",
                    metadata.partition(),
                    metadata.offset()
            );

            boolean received = false;
            long deadline =
                    System.nanoTime() + Duration.ofSeconds(20).toNanos();

            while (!received && System.nanoTime() < deadline) {

                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, String> record : records) {

                    if (record.value().contains(eventId)) {

                        assertEquals(vin, record.key());
                        assertEquals(
                                metadata.partition(),
                                record.partition()
                        );
                        assertEquals(
                                metadata.offset(),
                                record.offset()
                        );

                        System.out.printf(
                                "CONSUMIDO: partition=%d, offset=%d%n",
                                record.partition(),
                                record.offset()
                        );

                        received = true;
                        break;
                    }
                }
            }

            assertTrue(
                    received,
                    "No se recuperó el evento publicado"
            );
        }
    }
}
