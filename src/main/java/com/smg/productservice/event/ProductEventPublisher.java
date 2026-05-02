package com.smg.productservice.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    public static final String PRODUCT_CREATED_TOPIC = "product.created";

    private final KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;

    public ProductEventPublisher(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishProductCreated(ProductCreatedEvent event) {
        kafkaTemplate.send(
                PRODUCT_CREATED_TOPIC,
                event.productId().toString(),
                event
        );
    }
}