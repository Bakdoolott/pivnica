package com.github.bakdoolott.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    private final PaymentProperties properties;

    public KafkaTopicConfig(PaymentProperties properties) {
        this.properties = properties;
    }

    /**
     * Создаёт топик при старте, если его ещё нет.
     * 1 партиция достаточно для мока; в проде число партиций задаётся под нагрузку.
     */
    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(properties.getEventsTopic())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
