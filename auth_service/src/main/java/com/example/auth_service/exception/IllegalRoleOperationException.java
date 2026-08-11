package com.example.auth_service.exception;

public class IllegalRoleOperationException extends RuntimeException {
    public IllegalRoleOperationException(String message) {
        super(message);
    }
}
