package com.smg.productservice.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Publishes unpublished outbox events to Kafka.
 * Implements the outbox pattern to ensure reliable event delivery.
 */
@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        var events = outboxEventRepository.findTop10ByPublishedAtIsNullOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        log.info("Publishing {} outbox event(s)", events.size());

        for (OutboxEvent event : events) {
            kafkaTemplate.send(event.getTopic(), event.getEventKey(), event.getPayload());
            event.markPublished();

            log.info("Published outbox event id={} topic={} key={}",
                    event.getId(), event.getTopic(), event.getEventKey());
        }
    }
}