package com.github.bakdoolott.coreservice.models.dto;

import jakarta.validation.constraints.*;
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
    @Size(max = 50)
    List<Long> tableIds;

    @NotNull
    LocalDate date;

    @NotNull
    LocalTime startTime;

    @NotNull
    @Positive
    @Max(200)
    Integer guestCount;

    @Size(max = 500)
    String comment;

    @NotBlank
    @Size(min = 2, max = 100)
    String userName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Телефон в формате +996XXXXXXXXX")
    String phoneNumber;
}