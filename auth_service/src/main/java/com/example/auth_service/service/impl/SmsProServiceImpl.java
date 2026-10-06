package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.SmsProRequest;
import com.example.auth_service.service.SmsProService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmsProServiceImpl implements SmsProService {

    @Value("${nikita.sms.login}")
    private String login;
    @Value("${nikita.sms.password}")
    private String password;
    @Value("${nikita.sms.sender}")
    private String sender;
    @Value("${nikita.sms.test:false}")
    private boolean testMode;

    private final RestTemplate restTemplate;
    private final String API_URL;

    public SmsProServiceImpl() {
        this.restTemplate = new RestTemplate();
        API_URL = "https://smspro.nikita.kg/api/message";
    }


    @Override
    public SmsProRequest send(String phone, String text) {
        return send(List.of(phone), text);
    }

    @Override
    public SmsProRequest send(List<String> phones, String text) {
        if (!StringUtils.hasText(login) || !StringUtils.hasText(password) || !StringUtils.hasText(sender)) {
            log.warn("Nikita SMS: login/password/sender не заданы (проверь NIKITA_SMS_LOGIN, " +
                    "NIKITA_SMS_PASSWORD, NIKITA_SMS_SENDER) — шлюз почти наверняка отклонит запрос");
        }
        if (testMode) {
            log.warn("Nikita SMS: включён test-режим (nikita.sms.test=true) — " +
                    "сообщение НЕ будет реально отправлено и не тарифицируется");
        }

        String messageId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        String phoneXml = phones.stream()
                .map(p -> "<phone>" + p.replace("+", "") +"</phone>")
                .collect(Collectors.joining());

        String body = """
                <?xml version="1.0" encoding="UTF-8"?>
                <message>
                    <login>%s</login>
                    <pwd>%s</pwd>
                    <id>%s</id>
                    <sender>%s</sender>
                     <text>%s</text>
                     <phones>%s</phones>
                     %s
                </message>
                """.formatted(login, password, messageId, sender, escapeXml(text), phoneXml, testMode ? "<test>1</test>" : "");

        // ВАЖНО: кодируем тело в UTF-8 вручную и отправляем как byte[].
        // Если отдать RestTemplate обычную String с Content-Type без charset,
        // StringHttpMessageConverter по умолчанию закодирует её в ISO-8859-1,
        // и кириллица превратится в "???" — именно это и происходило.
        byte[] requestBytes = body.getBytes(StandardCharsets.UTF_8);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_XML);
        httpHeaders.setContentLength(requestBytes.length);

        ResponseEntity<byte[]> response = restTemplate.postForEntity(
                API_URL, new HttpEntity<>(requestBytes, httpHeaders), byte[].class);

        String responseXml = response.getBody() != null
                ? new String(response.getBody(), StandardCharsets.UTF_8)
                : null;

        log.info("Nikita SMS raw response (id={}): {}", messageId, responseXml);

        return parseResponse(responseXml);
    }

    private SmsProRequest parseResponse(String xml) {
        if (!StringUtils.hasText(xml)) {
            throw new IllegalStateException("Пустой ответ от Nikita SMS");
        }
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            int status = Integer.parseInt(textOf(doc, "status"));
            return new SmsProRequest(status == 0, status, textOf(doc, "message"));
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось разобрать ответ Nikita SMS: " + xml, e);
        }
    }

    private String textOf(Document doc, String tag) {
        NodeList nodes = doc.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent() : "";
    }

    private String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}