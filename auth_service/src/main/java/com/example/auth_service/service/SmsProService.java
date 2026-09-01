package com.example.auth_service.service;

import com.example.auth_service.dto.request.SmsProRequest;

import java.util.List;

public interface SmsProService {
    SmsProRequest send(String phone, String text);
    SmsProRequest send(List<String> phones, String text);
}
