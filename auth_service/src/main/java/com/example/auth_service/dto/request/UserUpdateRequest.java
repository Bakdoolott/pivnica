package com.example.auth_service.dto.request;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest extends UserRequest{
    Long id;

    @Override
    public String toString() {
        return "UserUpdateRequest{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roles=" + roles +
                ", email='" + email + '\'' +
                '}';
    }
}
