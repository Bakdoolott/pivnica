package com.github.bakdoolott.coreservice.models.dto;


import com.github.bakdoolott.coreservice.models.Booking;
import com.github.bakdoolott.coreservice.models.BookingPrice;
import com.github.bakdoolott.coreservice.models.enums.TableType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_price_line_tb")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
public class BookingPriceLine {
    @Id
    @SequenceGenerator(name = "booking_price_line_seq", sequenceName = "booking_price_line_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "booking_price_line_seq")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    Booking booking;

    @Column(name = "table_id", nullable = false)
    Long tableId;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", nullable = false, length = 32)
    TableType tableType;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_id", nullable = false)
    BookingPrice appliedPrice;

    public static BookingPriceLine of(Booking booking, Long tableId, TableType type, BookingPrice applied) {
        BookingPriceLine line = new BookingPriceLine();
        line.setBooking(booking);
        line.setTableId(tableId);
        line.setTableType(type);
        line.setUnitPrice(applied.getPrice());
        line.setAppliedPrice(applied);
        return line;
    }
}
