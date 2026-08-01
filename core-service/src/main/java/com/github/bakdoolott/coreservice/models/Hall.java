package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
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