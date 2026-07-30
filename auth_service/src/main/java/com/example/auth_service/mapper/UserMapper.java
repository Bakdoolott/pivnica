package com.example.auth_service.mapper;

import com.example.auth_service.dto.request.UserRequest;
import com.example.auth_service.entity.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    public abstract UserEntity toEntity(UserRequest request);

    public UserEntity toEntityWithChatId(UserRequest request, Long chatId) {
        if (request == null) {
            return null;
        }
        UserEntity entity = toEntity(request);
        entity.setChatId(chatId);
        return entity;
    }
}
