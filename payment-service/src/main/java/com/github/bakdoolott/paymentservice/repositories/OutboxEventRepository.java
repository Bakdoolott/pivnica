package com.github.bakdoolott.paymentservice.repositories;

import com.github.bakdoolott.paymentservice.models.OutboxEvent;
import com.github.bakdoolott.paymentservice.models.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
