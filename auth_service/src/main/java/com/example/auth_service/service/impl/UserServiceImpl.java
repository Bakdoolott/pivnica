package com.example.auth_service.service.impl;

import com.example.auth_service.entity.enums.RoleEnums;
import com.example.auth_service.entity.model.UserEntity;
import com.example.auth_service.exception.IllegalRoleOperationException;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.UserDetailsImpl;
import com.example.auth_service.service.UserService;
import com.example.auth_service.util.PhoneNumberNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
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
    public UserEntity findOrCreateByPhoneNumber(String phoneNumber) {
        String normalized = PhoneNumberNormalizer.normalize(phoneNumber);
        return userRepository.findByPhoneNumber(normalized)
                .orElseGet(() -> userRepository.save(
                        UserEntity.builder()
                                .phoneNumber(normalized)
                                .roles(Set.of(RoleEnums.USER))
                                .build()));
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
    public UserEntity updateRoles(Set<RoleEnums> roleEnums, Long id) {
        UserEntity existing = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        existing.getRoles().addAll(roleEnums);
        return userRepository.save(existing);
    }

    @Override
    @Transactional
    public UserEntity removeRoles(Set<RoleEnums> roleEnums, Long id) {
        UserEntity existing = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (roleEnums.contains(RoleEnums.OWNER) && userRepository.countByRole(RoleEnums.OWNER) <= 1) {
            throw new IllegalRoleOperationException("Нельзя снять роль OWNER у последнего владельца системы");
        }

        Set<RoleEnums> remaining = new HashSet<>(existing.getRoles());
        remaining.removeAll(roleEnums);
        if (remaining.isEmpty()) {
            throw new IllegalRoleOperationException("У пользователя должна остаться хотя бы одна роль");
        }

        existing.getRoles().removeAll(roleEnums);
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

