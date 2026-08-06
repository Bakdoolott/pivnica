package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hall_tb")
public class Hall {
    @Id
    @SequenceGenerator(
            name = "hall_seq",
            sequenceName = "hall_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hall_seq")
    Long id;

    @Column(nullable = false)
    Integer floor;

    @Column(name = "hall_number",nullable = false,unique = true)
    String hallNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    HallStatus hallStatus = HallStatus.ENABLE;

    @Column(nullable = false)
    boolean enable = true;
}