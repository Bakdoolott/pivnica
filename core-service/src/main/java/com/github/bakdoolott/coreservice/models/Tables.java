package com.github.bakdoolott.coreservice.models;

import com.github.bakdoolott.coreservice.models.enums.TableState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "table_tb",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_table_number_hall",
                        columnNames = {"id_hall_tb", "table_number"}
                )
        }
)
public class Tables {
    @Id
    @SequenceGenerator(
            name = "table_seq",
            sequenceName = "table_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "table_seq")
    Long id;

    @Column(name = "table_number", nullable = false)
    Integer tableNumber;

    Integer x;

    Integer y;

    @Column(name = "place_count",nullable = false)
    Integer placeCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_state", nullable = false)
    TableState tableState = TableState.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hall_tb", nullable = false)
    Hall hall;

    @Column(nullable = false)
    @Builder.Default
    boolean enable = true;
}
