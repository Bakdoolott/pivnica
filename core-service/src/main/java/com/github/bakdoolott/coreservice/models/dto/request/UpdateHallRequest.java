package com.github.bakdoolott.coreservice.models.dto.request;

import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateHallRequest(Long id,
                                @NotNull Integer floor,
                                @NotBlank String hallNumber,
                                @NotNull HallStatus hallStatus) {
}