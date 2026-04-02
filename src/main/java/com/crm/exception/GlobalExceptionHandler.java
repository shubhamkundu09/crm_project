// GlobalExceptionHandler.java (updated)
package com.crm.exception;

import com.crm.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Resource Not Found");
        errorDetails.put("details", ex.getMessage());

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                errorDetails,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDuplicateResource(
            DuplicateResourceException ex, HttpServletRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Duplicate Resource");
        errorDetails.put("details", ex.getMessage());

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                errorDetails,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                "Validation failed",
                HttpStatus.BAD_REQUEST.value(),
                validationErrors,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("error", "Internal Server Error");
        errorDetails.put("details", ex.getMessage());

        ApiResponse<Map<String, String>> response = ApiResponse.error(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                errorDetails,
                request.getRequestURI()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}