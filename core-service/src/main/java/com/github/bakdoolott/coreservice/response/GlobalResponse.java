package com.github.bakdoolott.coreservice.response;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
public class GlobalResponse {
    String code;
    Object value;

    public static GlobalResponse success(Object data) {
        return builder().code("200").value(data).build();
    }

    public static GlobalResponse created(Object data) {
        return builder().code("201").value(data).build();
    }

    public static GlobalResponse badRequest(Object data) {
        return builder().code("400").value(data).build();
    }

    public static GlobalResponse forbidden(Object data) {
        return builder().code("403").value(data).build();
    }

    public static GlobalResponse notFound(Object data) {
        return builder().code("404").value(data).build();
    }

    public static GlobalResponse conflict(Object data) {
        return builder().code("409").value(data).build();
    }

    public static GlobalResponse serverError(Object data) {
        return builder().code("500").value(data).build();
    }

    public ResponseEntity<GlobalResponse> toEntity() {
        HttpStatus status = HttpStatus.resolve(Integer.parseInt(code));
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body(this);
    }
}
