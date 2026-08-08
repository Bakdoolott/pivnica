package com.github.bakdoolott.paymentservice.models;

import com.github.bakdoolott.paymentservice.models.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@Table(name = "payment_tb")
public class Payment {
    @Id
    @SequenceGenerator(
            name = "payment_seq",
            sequenceName = "payment_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    Long id;

    @Column(name = "booking_id", nullable = false)
    Long bookingId;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "amount", nullable = false)
    BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    PaymentStatus status;

    @Column(name = "idempotency_key", unique = true, length = 100)
    String idempotencyKey;

    // Идентификатор транзакции у реального провайдера. Для мока не заполняется.
    @Column(name = "provider_ref", length = 100)
    String providerRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Version
    Long version;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
