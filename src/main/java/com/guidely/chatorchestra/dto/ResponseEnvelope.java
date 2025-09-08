package com.guidely.chatorchestra.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Common response envelope for all API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseEnvelope<T> {
    private boolean success;
    private T data;
    private ErrorPayload error;
    private Instant timestamp;

    public static <T> ResponseEnvelope<T> success(T data) {
        return ResponseEnvelope.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ResponseEnvelope<T> error(String code, String message) {
        return ResponseEnvelope.<T>builder()
                .success(false)
                .error(ErrorPayload.builder()
                        .code(code)
                        .message(message)
                        .build())
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ResponseEnvelope<T> error(String code, String message, Object details) {
        return ResponseEnvelope.<T>builder()
                .success(false)
                .error(ErrorPayload.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .timestamp(Instant.now())
                .build();
    }
}




