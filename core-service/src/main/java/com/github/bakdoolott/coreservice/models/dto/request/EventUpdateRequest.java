package com.github.bakdoolott.coreservice.models.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventUpdateRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;

}
