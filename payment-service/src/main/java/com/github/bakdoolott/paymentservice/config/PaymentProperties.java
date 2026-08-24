package com.github.bakdoolott.paymentservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    /** Топик, в который публикуются события об оплате. */
    private String eventsTopic = "payment.events";

    /** Валюта по умолчанию, если клиент не указал её в запросе. */
    private String defaultCurrency = "KGS";

    /** Режим имитации оплаты: ALWAYS_SUCCESS | AMOUNT_BASED. */
    private String mockMode = "ALWAYS_SUCCESS";
}
