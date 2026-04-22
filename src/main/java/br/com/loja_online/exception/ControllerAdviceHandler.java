package br.com.loja_online.exception;

import br.com.loja_online.service.exceptions.AuthenticationException;
import br.com.loja_online.service.exceptions.ConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import br.com.loja_online.service.exceptions.ObjectNotFoundException;

@ControllerAdvice
public class ControllerAdviceHandler {

  @ExceptionHandler(ObjectNotFoundException.class)
  public ResponseEntity<StandardError> objectNotFound(ObjectNotFoundException ex,
                                                      HttpServletRequest request) {
    StandardError standardError = new StandardError(
            System.currentTimeMillis(),
            404,
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
    );

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(standardError);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationError> methodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                       HttpServletRequest request) {
    ValidationError validationError = new ValidationError(
            System.currentTimeMillis(),
            400,
            "Validation Error",
            "Erro de validação",
            request.getRequestURI()
    );

    for(FieldError x: ex.getBindingResult().getFieldErrors()) {
      validationError.addError(x.getField(), x.getDefaultMessage());
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationError);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<StandardError> authenticationException(AuthenticationException ex,
                                                               HttpServletRequest request) {
    StandardError standardError = new StandardError(
            System.currentTimeMillis(),
            401,
            "Unauthorized",
            ex.getMessage(),
            request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(standardError);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<StandardError> conflictException(ConflictException ex,
                                                         HttpServletRequest request) {
    StandardError standardError = new StandardError(
            System.currentTimeMillis(),
            409,
            "Conflict",
            ex.getMessage(),
            request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.CONFLICT).body(standardError);
  }
}
