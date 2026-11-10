package com.github.bakdoolott.coreservice.mappers;

import com.github.bakdoolott.coreservice.models.PaymentQr;
import com.github.bakdoolott.coreservice.models.dto.response.PaymentQrResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentQrMapper {
    PaymentQrResponse toResponse(PaymentQr entity);
}