package com.example.auth_service.service.impl;

import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserEntity save(UserEntity entity) {
        return userRepository.findByPhone(entity.getPhone())
                .map(existing -> {
                    existing.setName(entity.getName());
                    existing.setChatId(entity.getChatId());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    entity.setRoles(List.of(roleRepository.findByRoleName("USER")
                            .orElseThrow(() -> new RuntimeException("Роль USER не найдена в БД"))));
                    return userRepository.save(entity);
                });
    }

    @Override
    public UserEntity findByPhone(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByPhone(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return UserDetailsImpl.build(user);
    }
}
