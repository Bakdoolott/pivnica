package com.github.bakdoolott.coreservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventCreateRequest {
    @NotBlank
    private String title;
    private String description;
    private String imageUrl;
    private String imageName;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
}