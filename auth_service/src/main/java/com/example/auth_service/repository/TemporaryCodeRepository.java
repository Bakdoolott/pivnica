package com.example.auth_service.repository;

import com.example.auth_service.entity.model.TemporaryCodeEntity;
import com.example.auth_service.entity.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemporaryCodeRepository extends JpaRepository<TemporaryCodeEntity, Long> {
    Optional<TemporaryCodeEntity> findByUser_IdAndEnableTrue(Long userId);
    Optional<TemporaryCodeEntity> findByUser_Id(Long id);
}
