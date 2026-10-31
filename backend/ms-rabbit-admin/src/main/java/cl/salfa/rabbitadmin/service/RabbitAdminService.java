package cl.salfa.rabbitadmin.service;

import cl.salfa.rabbitadmin.dto.CreateBindingRequest;
import cl.salfa.rabbitadmin.dto.CreateExchangeRequest;
import cl.salfa.rabbitadmin.dto.CreateQueueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RabbitAdminService {

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^dyn\\.[a-zA-Z0-9._-]{1,100}$");

    private static final Pattern ROUTING_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._*#-]{1,150}$");

    private final RabbitAdmin rabbitAdmin;

    public Map<String, Object> createQueue(
            CreateQueueRequest request
    ) {
        String name = validateName(request.name());

        String declared = rabbitAdmin.declareQueue(
                new Queue(name, true, false, false)
        );

        if (declared == null) {
            throw new IllegalStateException(
                    "RabbitMQ no confirmó la creación de la cola"
            );
        }

        return Map.of(
                "name", declared,
                "durable", true,
                "status", "created"
        );
    }

    public void deleteQueue(String name) {
        String validName = validateName(name);

        // No eliminar una cola si tiene consumidores o mensajes.
        rabbitAdmin.deleteQueue(validName, true, true);
    }

    public Map<String, String> createExchange(
            CreateExchangeRequest request
    ) {
        String name = validateName(request.name());

        String type = request.type()
                .trim()
                .toLowerCase(Locale.ROOT);

        switch (type) {
            case "direct" -> rabbitAdmin.declareExchange(
                    new DirectExchange(name, true, false)
            );

            case "topic" -> rabbitAdmin.declareExchange(
                    new TopicExchange(name, true, false)
            );

            default -> throw new IllegalArgumentException(
                    "Solo se permiten exchanges direct o topic"
            );
        }

        return Map.of(
                "name", name,
                "type", type,
                "status", "created"
        );
    }

    public void deleteExchange(String name) {
        String validName = validateName(name);

        boolean deleted = rabbitAdmin.deleteExchange(validName);

        if (!deleted) {
            throw new IllegalStateException(
                    "No fue posible eliminar el exchange"
            );
        }
    }

    public Map<String, String> createBinding(
            CreateBindingRequest request
    ) {
        Binding binding = toBinding(request);

        rabbitAdmin.declareBinding(binding);

        return Map.of(
                "queue", binding.getDestination(),
                "exchange", binding.getExchange(),
                "routingKey", binding.getRoutingKey(),
                "status", "created"
        );
    }

    public void deleteBinding(CreateBindingRequest request) {
        rabbitAdmin.removeBinding(toBinding(request));
    }

    private Binding toBinding(CreateBindingRequest request) {
        String queue = validateName(request.queue());
        String exchange = validateName(request.exchange());
        String routingKey = validateRoutingKey(
                request.routingKey()
        );

        return new Binding(
                queue,
                Binding.DestinationType.QUEUE,
                exchange,
                routingKey,
                null
        );
    }

    private String validateName(String value) {
        if (value == null ||
                !NAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "El nombre debe comenzar con dyn. y contener "
                            + "solo letras, números, puntos, guiones o guiones bajos"
            );
        }

        return value;
    }

    private String validateRoutingKey(String value) {
        if (value == null ||
                !ROUTING_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Routing key inválida"
            );
        }

        return value;
    }
}
