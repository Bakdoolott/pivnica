package com.github.bakdoolott.coreservice.config;

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
    int holdMinutes = 15;
    int maxDepthDays = 30;
    LocalTime defaultStartTime = LocalTime.of(20, 0);

    LocalTime closingTime = LocalTime.of(6,0);
    ZoneId clubZone = ZoneId.of("Asia/Bishkek");
}