package com.github.bakdoolott.paymentservice.models.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull(message = "bookingId обязателен")
    private Long bookingId;

    @NotNull(message = "amount обязателен")
    @DecimalMin(value = "0.01", message = "amount должен быть больше 0")
    private BigDecimal amount;

    // Необязательно: если не передан, берётся валюта по умолчанию из конфига.
    private String currency;

    // Ключ идемпотентности: повторный запрос с тем же ключом вернёт существующий платёж
    // вместо создания нового (защита от двойного клика / ретрая).
    private String idempotencyKey;
}
