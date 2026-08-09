package com.example.auth_service.service.impl;

import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.UserService;
import com.example.auth_service.util.PhoneNumberNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserEntity save(UserEntity entity) {
        
        entity.setPhoneNumber(PhoneNumberNormalizer.normalize(entity.getPhoneNumber()));

        return userRepository.findByPhoneNumber(entity.getPhoneNumber())
                .map(existing -> {
                    existing.setFirstName(entity.getFirstName());
                    existing.setTgUserName(entity.getTgUserName());
                    existing.setChatId(entity.getChatId());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    entity.setRoles(Set.of(RoleEnums.USER));
                    return userRepository.save(entity);
                });
    }

    @Override
    public UserEntity findByPhoneNumber(String phoneNumber) {
        String normalized = PhoneNumberNormalizer.normalize(phoneNumber);

        UserEntity user = userRepository.findByPhoneNumber(normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        if (user.isEnable()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    @Transactional
    public UserEntity update(UserEntity entity) {
        System.out.println(entity.toString());
        UserEntity existing = userRepository.findById(entity.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        existing.setFirstName(entity.getFirstName());
        existing.setLastName(entity.getLastName());
        existing.setEmail(entity.getEmail());
        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public String delete(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        user.setEnable(true);
        userRepository.save(user);
        return "Аккаунт удалён";
    }

    @Override
    public UserEntity findById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        if (user.isEnable()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserDetailsImpl.build(findByPhoneNumber(username));
    }
}

