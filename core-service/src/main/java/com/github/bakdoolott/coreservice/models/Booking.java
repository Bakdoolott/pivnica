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
import java.util.HashSet;
import java.util.Set;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "booking_table_tb",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "table_id")
    )
    Set<Tables> tables = new HashSet<>();

    @Column(name = "user_name", nullable = false)
    String userName;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_id", nullable = false)
    BookingPrice price;

    @Column(name = "cancel_reason", length = 300)
    String cancelReason;

    @Column(name = "cancelled_at")
    LocalDateTime cancelledAt;

    @Column(name = "cancelled_by")
    Long cancelledBy;

    @Column(nullable = false)
    boolean enable = true;

    @Version
    Long version;
}
