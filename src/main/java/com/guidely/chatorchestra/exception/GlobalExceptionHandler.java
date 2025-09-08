package com.guidely.chatorchestra.exception;

import com.guidely.chatorchestra.dto.ResponseEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global exception handler for the application
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseEnvelope<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage()));
        
        ResponseEnvelope<Void> response = ResponseEnvelope.error(
                "VALIDATION_ERROR", 
                "Validation failed", 
                errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseEnvelope<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ResponseEnvelope<Void> response = ResponseEnvelope.error(
                "INVALID_ARGUMENT", 
                ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ResponseEnvelope<Void>> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        
        ResponseEnvelope<Void> response = ResponseEnvelope.error(
                "RESOURCE_NOT_FOUND", 
                ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseEnvelope<Void>> handleIllegalStateException(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        
        ResponseEnvelope<Void> response = ResponseEnvelope.error(
                "INVALID_STATE", 
                ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseEnvelope<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ResponseEnvelope<Void> response = ResponseEnvelope.error(
                "INTERNAL_ERROR", 
                "An unexpected error occurred"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}




