package com.github.bakdoolott.coreservice.models.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventCreateRequest {
    @NotBlank
    private String title;
    private String description;
    @NotNull
    private MultipartFile image;
    @NotNull
    private LocalDateTime startsAt;
    @NotNull
    private LocalDateTime endsAt;
}