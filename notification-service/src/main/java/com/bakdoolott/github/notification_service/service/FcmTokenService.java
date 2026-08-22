package com.bakdoolott.github.notification_service.service;

import com.bakdoolott.github.notification_service.Entity.UserDevice;
import com.bakdoolott.github.notification_service.repositories.UserDeviceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FcmTokenService {

    private final UserDeviceRepo tokenRepository;

    @Autowired
    public FcmTokenService(UserDeviceRepo tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void saveOrUpdateToken(Long userId, String token, String deviceType) {
        UserDevice fcmToken = tokenRepository.findByDeviceToken(token)
                .orElse(new UserDevice());

        fcmToken.setUserId(userId);
        fcmToken.setDeviceToken(token);
        fcmToken.setDeviceType(deviceType);
        fcmToken.setUpdatedAt(LocalDateTime.now());

        tokenRepository.save(fcmToken);
    }

    @Transactional
    public void removeToken(String token) {
        tokenRepository.deleteByDeviceToken(token);
    }

    @Transactional(readOnly = true)
    public List<String> getDeviceTokensByUserId(Long userId) {
        return tokenRepository.findAllByUserId(userId).stream()
                .map(UserDevice::getDeviceToken)
                .toList();
    }
}
