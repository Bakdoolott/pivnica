package com.github.bakdoolott.coreservice.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "booking")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Validated
@Data
public class BookingProperties {

    @Min(1)
    int maxDepthDays = 30;

    @NotNull
    LocalTime openTime = LocalTime.of(18, 0);

    @NotNull
    LocalTime arrivalDeadline = LocalTime.of(21, 30);

    @Min(0)
    int minLeadMinutes = 30;

    @Min(0)
    int arrivalGraceMinutes = 15;

    @Min(0)
    int noShowSafetyMinutes = 5;

    @Min(0)
    int maxTablesPerBooking = 5;

    ZoneId clubZone = ZoneId.of("Asia/Bishkek");

    Set<DayOfWeek> peakDays = EnumSet.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    @Min(0)
    int maxFreeActiveBookingPerUser = 3;

    @Min(0)
    int maxFreeActiveBookingPerPhone = 3;

    @Min(0)
    int refundFullBeforeHours = 24;

    @Min(0) @Max(100)
    int refundPartialPercent = 50;

    @Min(0)
    int refundWorkingDays = 3;

    @Min(0)
    int cancellationGraceMinutes = 15;

    @Min(0)
    int cancellationGraceBeforeHours = 2;

    @AssertTrue(message = "arrivalDeadline должен быть позже openTime")
    public boolean isArrivalDeadlineValid() {
        if (arrivalDeadline == null || openTime == null) {
            return true;
        }
        return arrivalDeadline.isAfter(openTime);
    }
}