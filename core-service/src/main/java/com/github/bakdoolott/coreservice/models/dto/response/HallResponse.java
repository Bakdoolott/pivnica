package com.github.bakdoolott.coreservice.models.dto.response;

import com.github.bakdoolott.coreservice.models.enums.HallStatus;

public record HallResponse(
        Long id,
        Integer floor,
        String hallNumber,
        HallStatus hallStatus
) {
}
