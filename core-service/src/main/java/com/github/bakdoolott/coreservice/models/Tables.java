package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.TableStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@Table(name = "table_tb")
public class Tables {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "table_number", nullable = false)
    Integer tableNumber;

    Integer x;

    Integer y;

    @Column(name = "place_county",nullable = false)
    Integer placeCounty;

    @ManyToOne
    @JoinColumn(name = "id_hall_tb", nullable = false)
    private Hall hall;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_status",nullable = false)
    TableStatus tableStatus;
}
