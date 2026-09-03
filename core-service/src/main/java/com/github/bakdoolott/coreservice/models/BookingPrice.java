package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.TableType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_price_tb")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class BookingPrice {

    @Id
    @SequenceGenerator(name = "booking_price_seq", sequenceName = "booking_price_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_price_seq")
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", nullable = false, length = 32)
    TableType tableType;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal price;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "start_at", nullable = false)
    LocalDateTime startAt;

    @Column(name = "end_at")
    LocalDateTime endAt;

    @Column(nullable = false)
    boolean enable = true;

    @Version
    Long version;

    public boolean isOpen(){
        return enable && endAt == null;
    }
}