package com.example.craftsy;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

    // Validation errors (e.g., missing fields)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return ResponseEntity.badRequest().body(msg);
    }

    // Generic fallback
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        return ResponseEntity.status(500).body("Server error: " + ex.getMessage());
    }
}
