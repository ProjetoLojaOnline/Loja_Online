package br.com.loja_online.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.loja_online.service.exceptions.AuthenticationException;
import br.com.loja_online.service.exceptions.ConflictException;
import br.com.loja_online.service.exceptions.ForbiddenException;
import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@ControllerAdvice
public class ControllerAdviceHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> objectNotFound(ObjectNotFoundException ex, HttpServletRequest request) {
        StandardError standardError = new StandardError(
                System.currentTimeMillis(), 404, "Not Found", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> methodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        ValidationError validationError = new ValidationError(
                System.currentTimeMillis(), 400, "Bad Request", "Erro de validação", request.getRequestURI());

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            validationError.addError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardError> authenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        StandardError standardError = new StandardError(
                System.currentTimeMillis(), 401, "Unauthorized", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(standardError);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<StandardError> forbiddenException(ForbiddenException ex, HttpServletRequest request) {
        StandardError standardError = new StandardError(
                System.currentTimeMillis(), 403, "Forbidden", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(standardError);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<StandardError> conflictException(ConflictException ex, HttpServletRequest request) {
        StandardError standardError = new StandardError(
                System.currentTimeMillis(), 409, "Conflict", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(standardError);
    }
}
