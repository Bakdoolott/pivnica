package com.example.auth_service.util;

public class PhoneNumberNormalizer {
    public static String normalize(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            return rawPhone;
        }

        String cleaned = rawPhone.trim().replaceAll("[^0-9+]", "");
        boolean hadPlus = cleaned.startsWith("+");
        String digitsOnly = cleaned.replace("+", "");

        if (hadPlus) {
            return "+" + digitsOnly;
        }

        if (digitsOnly.startsWith("00")) {
            return "+" + digitsOnly.substring(2);
        }

        return "+" + digitsOnly;
    }

}
