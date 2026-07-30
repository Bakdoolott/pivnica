package com.example.auth_service.mapper;

import com.example.auth_service.dto.request.UserRequest;
import com.example.auth_service.entity.model.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserMapper {

    @Mapping(source = "phone", target = "phoneNumber")
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
