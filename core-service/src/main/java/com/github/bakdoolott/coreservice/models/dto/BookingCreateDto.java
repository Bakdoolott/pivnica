package com.github.bakdoolott.coreservice.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class BookingCreateDto {

    @NotNull
    Long hallId;

    @NotEmpty
    List<Long> tableIds;

    @NotNull
    LocalDate date;

    @NotNull
    LocalTime startTime;

    @NotNull
    @Positive
    Integer guestCount;

    String comment;

    @NotBlank
    String userName;

    @NotBlank
    String phoneNumber;
}