package com.github.bakdoolott.coreservice.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "[1, 2]", description = "Список ID столиков")
    Long hallId;

    @NotEmpty
    @Size(max = 50)
    List<Long> tableIds;

    @NotNull
    @Schema(example = "2026-08-25", description = "Дата брони")
    LocalDate date;

    @NotNull
    @Schema(example = "20:00", description = "Время начала (HH:mm)")
    LocalTime startTime;

    @NotNull
    @Positive
    @Max(200)
    @Schema(example = "4", description = "Количество гостей")
    Integer guestCount;

    @Size(max = 500)
    @Schema(example = "Test booking", description = "Комментарий (необязательно)")
    String comment;

    @NotBlank
    @Size(min = 2, max = 100)
    @Schema(example = "John Doe", description = "Имя гостя")
    String userName;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Телефон в формате +996XXXXXXXXX")
    @Schema(example = "+996555123456", description = "Номер телефона")
    String phoneNumber;
}