package com.github.bakdoolott.coreservice.config;

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
    LocalTime defaultStartTime = LocalTime.of(20, 0);
    @NotNull
    LocalTime closingTime = LocalTime.of(6,0);
    @NotNull
    ZoneId clubZone = ZoneId.of("Asia/Bishkek");
}