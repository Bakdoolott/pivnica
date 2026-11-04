package com.github.bakdoolott.coreservice.models;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_qr_tb")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class PaymentQr {

    @Id
    @SequenceGenerator(name = "payment_qr_seq", sequenceName = "payment_qr_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_qr_seq")
    Long id;

    @Column(name = "image_url", nullable = false, length = 500)
    String imageUrl;

    @Column(length = 255)
    String description;

    @Column(nullable = false)
    boolean enable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "created_by")
    Long createdBy;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Column(name = "updated_by")
    Long updatedBy;

    @Version
    Long version;
}