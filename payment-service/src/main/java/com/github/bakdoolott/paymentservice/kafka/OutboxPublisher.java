package com.github.bakdoolott.paymentservice.kafka;

import com.github.bakdoolott.paymentservice.models.OutboxEvent;
import com.github.bakdoolott.paymentservice.models.enums.OutboxStatus;
import com.github.bakdoolott.paymentservice.repositories.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Публикует накопленные в outbox события в Kafka.
 * Забирает пачку PENDING-записей, отправляет синхронно, помечает SENT.
 * Если Kafka недоступна — запись остаётся PENDING и будет повторена на следующем тике.
 */
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${payment.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> batch = outboxRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, BATCH_SIZE));

        if (batch.isEmpty()) {
            return;
        }

        log.debug("Outbox: найдено {} событий к отправке", batch.size());

        for (OutboxEvent event : batch) {
            try {
                // Ключ = aggregateId (id платежа) -> события одной брони не переставятся местами.
                kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get();

                event.setStatus(OutboxStatus.SENT);
                event.setSentAt(LocalDateTime.now());
                log.info("Outbox: событие id={} ({}) опубликовано в топик {}",
                        event.getId(), event.getEventType(), event.getTopic());
            } catch (Exception e) {
                // Оставляем PENDING — повторим на следующем тике. Прерываем пачку,
                // чтобы не молотить в недоступный брокер.
                log.warn("Outbox: не удалось опубликовать событие id={}, повтор позже. Причина: {}",
                        event.getId(), e.getMessage());
                break;
            }
        }
    }
}
