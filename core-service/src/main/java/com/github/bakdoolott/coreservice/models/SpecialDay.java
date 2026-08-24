package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.DayType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@Table(name = "special_day_tb")
public class SpecialDay {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "special_day_seq")
    @SequenceGenerator(name = "special_day_seq",sequenceName = "special_day_seq",allocationSize = 50)
    Long id;

    @Column(name = "day_date",nullable = false)
    LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    DayType dayType;

    @Column(nullable = false,length = 50)
    String name;

    @Column(nullable = false)
    boolean closed = false;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    LocalDateTime createdAt;

    @Column(name = "created_by")
    Long createdBy;

    @Column(nullable = false)
    boolean enable = true;

    @Version
    Long version;





}
