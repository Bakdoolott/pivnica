package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.BookingStatus;
import com.github.bakdoolott.coreservice.models.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@Table(name = "book_tb")
public class Booking {
    @Id
    @SequenceGenerator(
            name = "booking_seq",
            sequenceName = "booking_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_seq")
    Long id;

    @Column(name = "user_id",nullable = false)
    Long userId;

    @Column(name = "date_time",nullable = false)
    LocalDateTime dateTime;

    @Column(name = "ends_at", nullable = false)
    LocalDateTime endsAt;

    @Column(name = "guest_count", nullable = false)
    Integer guestCount;

    @Column(name = "comment", length = 500)
    String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "hold_until")
    LocalDateTime holdUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status",nullable = false)
    PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    BookingStatus bookingStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_table_tb", nullable = false)
    Tables tables;

    @Column(name = "user_name", nullable = false)
    String userName;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;

    @Column(nullable = false)
    boolean enable = true;

    @Version
    Long version;
}
