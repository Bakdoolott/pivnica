package com.example.auth_service.service.impl;

import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.UserService;
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
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        if (!user.isEnable()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserDetailsImpl.build(findByPhoneNumber(username));
    }
}

