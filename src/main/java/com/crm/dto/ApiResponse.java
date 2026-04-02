// ApiResponse.java
package com.crm.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private int statusCode;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    // Success response with data
    public static <T> ApiResponse<T> success(T data, String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .data(data)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    // Success response without data
    public static <T> ApiResponse<T> success(String message, String path) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .statusCode(200)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    // Error response
    public static <T> ApiResponse<T> error(String message, int statusCode, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    // Error response with data
    public static <T> ApiResponse<T> error(String message, int statusCode, T data, String path) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .statusCode(statusCode)
                .data(data)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}