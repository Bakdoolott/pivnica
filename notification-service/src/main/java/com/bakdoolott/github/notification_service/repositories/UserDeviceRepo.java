package com.bakdoolott.github.notification_service.repositories;

import com.bakdoolott.github.notification_service.Entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepo extends JpaRepository<UserDevice,Long> {
    List<UserDevice> findAllByUserId(Long userId);
    Optional<UserDevice> findByDeviceToken(String deviceToken);
    void deleteByDeviceToken(String deviceToken);
}
