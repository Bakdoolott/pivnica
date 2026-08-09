package com.github.bakdoolott.coreservice.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

@Component
@ConfigurationProperties(prefix = "booking")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class BookingProperties {
    @Min(1)
    int holdMinutes = 15;

    @Min(1)
    int maxDepthDays = 30;

    @NotNull
    LocalTime openTime = LocalTime.of(18, 0);

    @NotNull
    LocalTime closingTime = LocalTime.of(4,0);

    @Min(0) @Max(1440)
    int minLeadMinutes = 30;

    @Min(30) @Max(720)
    int minDurationMinutes = 60;

    @Min(1) @Max(50)
    int maxTablesPerBooking = 10;

    public boolean isOvernight() {
        return !closingTime.isAfter(openTime);
    }
    @NotNull
    ZoneId clubZone = ZoneId.of("Asia/Bishkek");
}