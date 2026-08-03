package com.github.bakdoolott.coreservice.advice;

import com.github.bakdoolott.coreservice.exceptions.ConflictExceptions;
import com.github.bakdoolott.coreservice.exceptions.LogicExceptions;
import com.github.bakdoolott.coreservice.exceptions.NotFoundExceptions;
import com.github.bakdoolott.coreservice.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundExceptions.class)
    public ResponseEntity<GlobalResponse> notFound(NotFoundExceptions exception) {
        log.warn("Not found: {}", exception.getMessage());
        return GlobalResponse.notFound(exception.getMessage()).toEntity();
    }

    @ExceptionHandler(ConflictExceptions.class)
    public ResponseEntity<GlobalResponse> conflict(ConflictExceptions exception) {
        log.warn("Conflict: {}", exception.getMessage());
        return GlobalResponse.conflict(exception.getMessage()).toEntity();
    }

    @ExceptionHandler(LogicExceptions.class)
    public ResponseEntity<GlobalResponse> logicError(LogicExceptions exception) {
        log.warn("Logic error: {}", exception.getMessage());
        return GlobalResponse.badRequest(exception.getMessage()).toEntity();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalResponse> accessDenied(AccessDeniedException exception) {
        log.warn("Access denied: {}", exception.getMessage());
        return GlobalResponse.forbidden("Доступ запрещён").toEntity();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (old, newVal) -> old
                ));
        log.warn("Validation failed: {}", errors);
        return GlobalResponse.badRequest(errors).toEntity();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GlobalResponse> internalError(RuntimeException exception) {
        log.error("Internal server error", exception);
        return GlobalResponse.serverError("Internal Server Error").toEntity();
    }
    }
