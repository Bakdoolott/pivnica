package com.example.auth_service.dto.request;

import com.example.auth_service.entity.enums.RoleEnums;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
        String firstName;
        String lastName;
        Set<RoleEnums> roles;
        @Email(message = "Некорректный формат email")
        String email;
}
