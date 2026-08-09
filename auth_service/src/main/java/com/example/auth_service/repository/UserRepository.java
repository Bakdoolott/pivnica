package com.example.auth_service.repository;

import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.entity.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPhoneNumber(String phoneNumber);

    @Query("SELECT COUNT(u) FROM UserEntity u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") RoleEnums role);
}
