package com.github.bakdoolott.coreservice.models.dto;

import com.github.bakdoolott.coreservice.models.enums.HallStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class HallDto {
    Long id;

    Integer floor;

    String hallNumber;

    HallStatus hallStatus = HallStatus.ENABLE;

    boolean enable = true;
}