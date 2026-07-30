package com.example.auth_service.service.impl;

import com.example.auth_service.bot.LoginTelegramBot;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.security.JwtCore;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.AuthService;
import com.example.auth_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class AuthServiceImpl implements AuthService {

    private StringRedisTemplate redisTemplate;
    private LoginTelegramBot loginTelegramBot;
    private UserService userService;
    private JwtCore jwtCore;

    private final Duration codeTTL;
    private final SecureRandom random;

    @Autowired
    public AuthServiceImpl(StringRedisTemplate redisTemplate,
                           LoginTelegramBot loginTelegramBot,
                           UserService userService,
                           JwtCore jwtCore) {
        this.redisTemplate = redisTemplate;
        this.loginTelegramBot = loginTelegramBot;
        this.userService = userService;
        this.jwtCore = jwtCore;
        this.codeTTL = Duration.ofMinutes(2);
        this.random = new SecureRandom();
    }


    @Override
    public void generateAndSend(String phone, Long chatId) {
        String code = generatedCode();
        redisTemplate.opsForValue().set("authCode:" + phone, code, codeTTL);
        loginTelegramBot.sendTextMessage(chatId, "Ваш код: " + code);
    }

    @Override
    public String verify(String phone, String code){
        try {
            String key = "authCode:" + phone;
            String savedCode = redisTemplate.opsForValue().get(key);

            if (savedCode == null) {
                throw new RuntimeException("Неверный или истёкший код");
            }

            if (!savedCode.equals(code)) {
                throw new RuntimeException("Неверный код");
            }
            redisTemplate.delete(key);
            return jwtCore.jwtGenerator(UserDetailsImpl.build(userService.findByPhone(phone)));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String login(UserEntity userEntity) {
        UserEntity entity = userService.findByPhone(userEntity.getPhone());
        generateAndSend(entity.getPhone(), entity.getChatId());
        return "Отправлен код в Telegram";
    }

    @Override
    public UserEntity getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String phone;

        if (principal instanceof UserDetails) {
            phone = ((UserDetails) principal).getUsername();
        } else {
            phone = principal.toString();
        }

        return userService.findByPhone(phone);
    }

    private String generatedCode(){
        Integer number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }


}
