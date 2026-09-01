package com.example.auth_service.service.impl;

import com.example.auth_service.dto.response.TokenResponse;
import com.example.auth_service.entity.model.TemporaryCodeEntity;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.repository.TemporaryCodeRepository;
import com.example.auth_service.security.JwtCore;
import com.example.auth_service.security.RefreshTokenService;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.SmsProService;
import com.example.auth_service.service.UserService;
import com.example.auth_service.util.PhoneNumberNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${auth.code.ttl}")
    private Duration codeTtl;

    @Value("${auth.code.resend-interval}")
    private Duration resendInterval;

    @Value("${auth.code.max-attempts}")
    private int maxAttempts;

    @Value("${auth.test-account.phone:}")
    private String testPhone;

    @Value("${auth.test-account.code:}")
    private String testCode;

    private final TemporaryCodeRepository temporaryCodeRepository;
    private final RefreshTokenService refreshTokenService;
    private final SmsProService smsProService;
    private final UserService userService;
    private final JwtCore jwtCore;
    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void generateAndSend(String phoneNumber) {
        String phone = PhoneNumberNormalizer.normalize(phoneNumber);

        temporaryCodeRepository.findByPhoneNumberAndEnableTrue(phone)
                .ifPresent(active -> {
                    if (Duration.between(active.getCreatedAt(), Instant.now()).compareTo(resendInterval) < 0) {
                        throw new RuntimeException("Подождите перед повторной отправкой кода");
                    }
                    active.setEnable(false);
                    temporaryCodeRepository.save(active);
                });

        String code = generatedCode();
        Instant now = Instant.now();

        temporaryCodeRepository.save(TemporaryCodeEntity.builder()
                .phoneNumber(phone)
                .code(code)
                .createdAt(now)
                .expiresAt(now.plus(codeTtl))
                .attempts(0)
                .enable(true)
                .build());

        smsProService.send(phone.substring(1), "Ваш код: " + code);
    }

    @Override
    @Transactional
    public TokenResponse verify(String phoneNumber, String code) {
        String phone = PhoneNumberNormalizer.normalize(phoneNumber);

        if (StringUtils.hasText(testPhone) && phone.equals(testPhone)) {
            if (!code.equals(testCode)) {
                throw new RuntimeException("Неверный код");
            }
            return issueTokenPair(userService.findOrCreateByPhoneNumber(phone));
        }

        TemporaryCodeEntity active = temporaryCodeRepository.findByPhoneNumberAndEnableTrue(phone)
                .orElseThrow(() -> new RuntimeException("Неверный или истёкший код"));

        if (active.getExpiresAt().isBefore(Instant.now())) {
            active.setEnable(false);
            temporaryCodeRepository.save(active);
            throw new RuntimeException("Код истёк, запросите новый");
        }

//        if (active.getAttempts() >= maxAttempts) {
//            active.setEnable(false);
//            temporaryCodeRepository.save(active);
//            throw new RuntimeException("Превышено число попыток, запросите новый код");
//        }

        if (!active.getCode().equals(code)) {
            active.setAttempts(active.getAttempts() + 1);
            temporaryCodeRepository.save(active);
            throw new RuntimeException("Неверный код");
        }

        active.setEnable(false);
        temporaryCodeRepository.save(active);

        UserEntity user = userService.findOrCreateByPhoneNumber(phone);
        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        UserEntity ownerAtIssueTime = refreshTokenService.rotate(refreshToken);
        UserEntity user = userService.findByPhoneNumber(ownerAtIssueTime.getPhoneNumber());

        return issueTokenPair(user);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    @Override
    public String login(UserEntity userEntity) {
        UserEntity existing = userService.findByPhoneNumber(userEntity.getPhoneNumber());
        generateAndSend(existing.getPhoneNumber());
        return "Отправлен код";
    }

    @Override
    public UserEntity getCurrentUser() {
        UserDetailsImpl principal = (UserDetailsImpl)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.findById(principal.getId());
    }

    @Override
    public String registration(String phone) {
        generateAndSend(phone);
        return "Отправлен код";
    }

    private TokenResponse issueTokenPair(UserEntity user) {
        String accessToken = jwtCore.jwtGenerator(UserDetailsImpl.build(user));
        String refreshToken = refreshTokenService.issue(user);
        return new TokenResponse(accessToken, refreshToken);
    }

    private String generatedCode() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}