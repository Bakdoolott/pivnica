package com.example.auth_service.service.impl;

import com.example.auth_service.dto.request.SmsProRequest;
import com.example.auth_service.service.SmsProService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_XML);

        ResponseEntity<String> response = restTemplate.postForEntity(
                API_URL, new HttpEntity<>(body, httpHeaders), String.class);

        return parseResponse(response.getBody());
    }

    private SmsProRequest parseResponse(String xml) {
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
