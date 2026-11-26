package cl.salfa.kafkaadmin.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaAdminService {

    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    public List<String> listTopics() throws Exception {

        return adminClient.listTopics()
                .names()
                .get(10, TimeUnit.SECONDS)
                .stream()
                .sorted()
                .toList();
    }

    public Map<String, Object> describeTopic(
            String topic
    ) throws Exception {

        var description = adminClient
                .describeTopics(List.of(topic))
                .allTopicNames()
                .get(10, TimeUnit.SECONDS)
                .get(topic);

        var resource = new ConfigResource(
                ConfigResource.Type.TOPIC,
                topic
        );

        var config = adminClient
                .describeConfigs(List.of(resource))
                .all()
                .get(10, TimeUnit.SECONDS)
                .get(resource);

        List<Map<String, Object>> partitions =
                new ArrayList<>();

        for (var partition : description.partitions()) {

            Map<String, Object> details =
                    new LinkedHashMap<>();

            details.put("partition", partition.partition());

            details.put(
                    "leader",
                    partition.leader() == null
                            ? null
                            : partition.leader().id()
            );

            details.put(
                    "replicas",
                    partition.replicas()
                            .stream()
                            .map(node -> node.id())
                            .toList()
            );

            details.put(
                    "isr",
                    partition.isr()
                            .stream()
                            .map(node -> node.id())
                            .toList()
            );

            partitions.add(details);
        }

        Map<String, String> settings = new LinkedHashMap<>();

        for (String name : List.of(
                "retention.ms",
                "cleanup.policy",
                "min.insync.replicas"
        )) {

            ConfigEntry entry = config.get(name);

            if (entry != null) {
                settings.put(name, entry.value());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("topic", topic);
        result.put("partitionCount", partitions.size());

        result.put(
                "replicationFactor",
                description.partitions().isEmpty()
                        ? 0
                        : description.partitions()
                        .getFirst()
                        .replicas()
                        .size()
        );

        result.put("partitions", partitions);
        result.put("config", settings);

        return result;
    }

    public Map<String, Object> createTopic(
            String topic,
            long retentionMs
    ) throws Exception {

        validateDemoTopic(topic);
        validateRetention(retentionMs);

        NewTopic newTopic = new NewTopic(
                topic,
                3,
                (short) 3
        );

        newTopic.configs(Map.of(
                "min.insync.replicas", "2",
                "cleanup.policy", "delete",
                "retention.ms", String.valueOf(retentionMs)
        ));

        adminClient.createTopics(List.of(newTopic))
                .all()
                .get(10, TimeUnit.SECONDS);

        return Map.of(
                "topic", topic,
                "status", "CREATED",
                "partitions", 3,
                "replicationFactor", 3
        );
    }

    public Map<String, Object> updateRetention(
            String topic,
            long retentionMs
    ) throws Exception {

        validateDemoTopic(topic);
        validateRetention(retentionMs);

        ConfigResource resource = new ConfigResource(
                ConfigResource.Type.TOPIC,
                topic
        );

        AlterConfigOp operation = new AlterConfigOp(
                new ConfigEntry(
                        "retention.ms",
                        String.valueOf(retentionMs)
                ),
                AlterConfigOp.OpType.SET
        );

        adminClient.incrementalAlterConfigs(
                Map.of(resource, List.of(operation))
        ).all().get(10, TimeUnit.SECONDS);

        return Map.of(
                "topic", topic,
                "retentionMs", retentionMs,
                "status", "UPDATED"
        );
    }

    public Map<String, Object> deleteTopic(
            String topic
    ) throws Exception {

        validateDemoTopic(topic);

        adminClient.deleteTopics(List.of(topic))
                .all()
                .get(10, TimeUnit.SECONDS);

        return Map.of(
                "topic", topic,
                "status", "DELETE_ACCEPTED"
        );
    }

    public List<String> listGroups() throws Exception {

        return adminClient.listConsumerGroups()
                .all()
                .get(10, TimeUnit.SECONDS)
                .stream()
                .map(group -> group.groupId())
                .sorted()
                .toList();
    }

    public Map<String, Object> describeGroup(
            String groupId
    ) throws Exception {

        var description = adminClient
                .describeConsumerGroups(List.of(groupId))
                .all()
                .get(10, TimeUnit.SECONDS)
                .get(groupId);

        var offsets = adminClient
                .listConsumerGroupOffsets(groupId)
                .partitionsToOffsetAndMetadata()
                .get(10, TimeUnit.SECONDS);

        Set<TopicPartition> partitions = new HashSet<>(
                offsets.keySet()
        );

        description.members().forEach(member ->
                partitions.addAll(
                        member.assignment().topicPartitions()
                )
        );

        Map<TopicPartition, OffsetSpec> requests =
                new HashMap<>();

        partitions.forEach(partition ->
                requests.put(
                        partition,
                        OffsetSpec.latest()
                )
        );

        var latest = requests.isEmpty()
                ? Map.<TopicPartition,
                org.apache.kafka.clients.admin
                        .ListOffsetsResult
                        .ListOffsetsResultInfo>of()
                : adminClient
                .listOffsets(requests)
                .all()
                .get(10, TimeUnit.SECONDS);

        List<Map<String, Object>> results =
                new ArrayList<>();

        for (TopicPartition partition : partitions) {

            var committed = offsets.get(partition);

            long endOffset = latest.get(partition).offset();

            Map<String, Object> row = new LinkedHashMap<>();

            row.put("topic", partition.topic());
            row.put("partition", partition.partition());

            row.put(
                    "currentOffset",
                    committed == null
                            ? null
                            : committed.offset()
            );

            row.put("logEndOffset", endOffset);

            row.put(
                    "lag",
                    committed == null
                            ? null
                            : endOffset - committed.offset()
            );

            results.add(row);
        }

        return Map.of(
                "groupId", groupId,
                "state", description.state().toString(),
                "members", description.members().size(),
                "partitions", results
        );
    }

    private void validateDemoTopic(String topic) {

        if (topic == null ||
                !topic.matches(
                        "salfa360\\.admin\\.[a-zA-Z0-9._-]{1,80}"
                )) {

            throw new IllegalArgumentException(
                    "Solo se permiten modificaciones en topicos " +
                            "con prefijo salfa360.admin."
            );
        }
    }

    private void validateRetention(long retentionMs) {

        if (retentionMs < 60000 ||
                retentionMs > 2592000000L) {

            throw new IllegalArgumentException(
                    "La retencion debe estar entre " +
                            "60000 y 2592000000 milisegundos."
            );
        }
    }
}