package com.github.bakdoolott.coreservice.booking;

import com.github.bakdoolott.coreservice.booking.enums.PaymentStatus;
import com.github.bakdoolott.coreservice.table.Tables;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "user_id",nullable = false)
    Long userId;

    @Column(name = "dateTime",nullable = false)
    LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status",nullable = false)
    PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "id_table_tb", nullable = false)
    private Tables tables;

    @Column(name = "user_name", nullable = false)
    String userName;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;



}
