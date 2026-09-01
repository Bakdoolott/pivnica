package com.example.auth_service.entity.model;

import com.example.auth_service.entity.enums.RoleEnums;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.Set;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_tb")
public class UserEntity extends BaseEntity {

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "tg_user_name")
    private String tgUserName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Builder.Default
    @Column(nullable = false)
    private boolean enable = false;

    @ElementCollection(targetClass = RoleEnums.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<RoleEnums> roles;

    @Override
    public String toString() {
        return "UserEntity{" +
                "phoneNumber='" + phoneNumber + '\'' +
                ", tgUserName='" + tgUserName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", enable=" + enable +
                ", roles=" + roles +
                ", id=" + id +
                '}';
    }
}
