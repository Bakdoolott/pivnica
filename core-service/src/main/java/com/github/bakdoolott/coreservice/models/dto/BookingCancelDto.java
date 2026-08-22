package com.github.bakdoolott.coreservice.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookingCancelDto(
        @NotBlank
        @Size(min = 3, max = 300)
        String reason,
        boolean fullRefund
) {
}