package com.example.auth_service.repository;

import com.example.auth_service.entity.model.RefreshTokenEntity;
import com.example.auth_service.entity.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenHashAndRevokedFalse(String tokenHash);
    Optional<RefreshTokenEntity> findByUser(UserEntity user);
}
