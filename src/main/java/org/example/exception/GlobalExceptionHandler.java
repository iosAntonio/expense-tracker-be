package org.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException exception) {

        Map<String, Object> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        errors.put("message", "Dati non validi. " + exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; ")));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        Map<String, String> errors = new HashMap<>();

        errors.put(
                "message",
                "Il formato della richiesta non è valido o contiene valori non ammessi."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(
            Exception exception) {

        logger.error("Unexpected error occurred", exception);

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Si è verificato un errore interno del server.");

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errors);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                errors.put(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        errors.put("message", "Dati non validi. " + exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .distinct()
                .collect(Collectors.joining("; ")));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException exception) {

        Map<String, String> errors = new HashMap<>();

        errors.put(
                "message",
                exception.getReason() != null ? exception.getReason() : "Richiesta non valida."
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParameter(
            MissingServletRequestParameterException exception) {

        Map<String, String> errors = new HashMap<>();

        errors.put(
                "message",
                "Parametro obbligatorio mancante: " + exception.getParameterName() + "."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        Map<String, String> errors = new HashMap<>();

        errors.put(
                "message",
                "Valore non valido per il parametro " + exception.getName() + "."
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
}
