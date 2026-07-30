package com.example.auth_service.service.impl;

import com.example.auth_service.bot.LoginTelegramBot;
import com.example.auth_service.entity.model.TemporaryCodeEntity;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.repository.TemporaryCodeRepository;
import com.example.auth_service.security.JwtCore;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final Duration CODE_TTL;
    private final Duration RESEND_INTERVAL;
    private final int MAX_ATTEMPTS;

    private final TemporaryCodeRepository temporaryCodeRepository;
    private final LoginTelegramBot loginTelegramBot;
    private final UserService userService;
    private final JwtCore jwtCore;
    private final SecureRandom random;

    @Autowired
    public AuthServiceImpl(TemporaryCodeRepository temporaryCodeRepository,
                           LoginTelegramBot loginTelegramBot,
                           UserService userService,
                           JwtCore jwtCore) {
        this.temporaryCodeRepository = temporaryCodeRepository;
        this.loginTelegramBot = loginTelegramBot;
        this.userService = userService;
        this.jwtCore = jwtCore;
        this.random = new SecureRandom();
        CODE_TTL = Duration.ofMinutes(2);
        RESEND_INTERVAL = Duration.ofSeconds(60);
        MAX_ATTEMPTS = 5;
    }

    @Override
    @Transactional
    public void generateAndSend(String phoneNumber, Long chatId) {
        UserEntity user = userService.findByPhoneNumber(phoneNumber);

        temporaryCodeRepository.findByUser_IdAndEnableTrue(user.getId())
                .ifPresent(active -> {
                    if (Duration.between(active.getCreatedAt(), Instant.now()).compareTo(RESEND_INTERVAL) < 0) {
                        throw new RuntimeException("Подождите перед повторной отправкой кода");
                    }
                    // Гасим предыдущий активный код — иначе вставка новой
                    // строки упадёт на partial unique индексе (один активный
                    // код на пользователя).
                    active.setEnable(false);
                    temporaryCodeRepository.save(active);
                });

        String code = generatedCode();
        Instant now = Instant.now();

        temporaryCodeRepository.save(TemporaryCodeEntity.builder()
                .user(user)
                .code(code)
                .createdAt(now)
                .expiresAt(now.plus(CODE_TTL))
                .attempts(0)
                .enable(true)
                .build());

        loginTelegramBot.sendTextMessage(chatId, "Ваш код: " + code);
    }

    @Override
    @Transactional
    public String verify(String phoneNumber, String code) {
        UserEntity user = userService.findByPhoneNumber(phoneNumber);

        TemporaryCodeEntity active = temporaryCodeRepository.findByUser_IdAndEnableTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("Неверный или истёкший код"));

        if (active.getExpiresAt().isBefore(Instant.now())) {
            active.setEnable(false);
            temporaryCodeRepository.save(active);
            throw new RuntimeException("Код истёк, запросите новый");
        }

        if (active.getAttempts() >= MAX_ATTEMPTS) {
            active.setEnable(false);
            temporaryCodeRepository.save(active);
            throw new RuntimeException("Превышено число попыток, запросите новый код");
        }

        if (!active.getCode().equals(code)) {
            active.setAttempts(active.getAttempts() + 1);
            temporaryCodeRepository.save(active);
            throw new RuntimeException("Неверный код");
        }

        active.setEnable(false);
        temporaryCodeRepository.save(active);

        return jwtCore.jwtGenerator(UserDetailsImpl.build(user));
    }

    @Override
    public String login(UserEntity userEntity) {
        UserEntity entity = userService.findByPhoneNumber(userEntity.getPhoneNumber());
        generateAndSend(entity.getPhoneNumber(), entity.getChatId());
        return "Отправлен код в Telegram";
    }

    @Override
    public UserEntity getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String phoneNumber = principal instanceof UserDetails
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        return userService.findByPhoneNumber(phoneNumber);
    }

    private String generatedCode() {
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

}
