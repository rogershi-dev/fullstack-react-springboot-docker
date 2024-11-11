package dev.uniqueman.fullstack_app_spring_boot.Controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import dev.uniqueman.fullstack_app_spring_boot.Entity.ApiError;
import dev.uniqueman.fullstack_app_spring_boot.Entity.ErrorCode;
import dev.uniqueman.fullstack_app_spring_boot.Exception.JwtAuthenticationException;
import dev.uniqueman.fullstack_app_spring_boot.Exception.TokenManagementException;
import dev.uniqueman.fullstack_app_spring_boot.Exception.UsernameAlreadyExistsException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TokenManagementException.class)
    public ResponseEntity<Object> handleTokenManagementException(TokenManagementException ex) {
        logger.warn("Error Code: {}, Error Message: {}", ErrorCode.TOKEN_MANAGEMENT_ERROR, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.TOKEN_MANAGEMENT_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }

    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<Object> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        logger.warn("Error Code: {}, Error Message: {}", ErrorCode.JWT_AUTHENTICATION_FAILED, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.JWT_AUTHENTICATION_FAILED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(AuthenticationException.class) 
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Error Code: {}, Error Message: {}", ErrorCode.USER_AUTHENTICATION_FAILED, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.USER_AUTHENTICATION_FAILED, ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        logger.warn("Error Code: {}, Error Message: {}", ErrorCode.USERNAME_NOT_FOUND, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.USERNAME_NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);
    }
    
    @ExceptionHandler(UsernameAlreadyExistsException.class) 
    public ResponseEntity<Object> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex) {
        logger.warn("Error Code: {}, Error Message: {}", ErrorCode.USERNAME_ALREADY_EXISTS, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.USERNAME_ALREADY_EXISTS, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentValidationExceptions(MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        for (FieldError error: ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String errorMessage = errors.toString();
        logger.warn("Error Code: {}, Error Message: {}", ErrorCode.FIELD_VALIDATION_FAILED, errorMessage);

        ApiError apiError = new ApiError(ErrorCode.FIELD_VALIDATION_FAILED, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class) 
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("Error Code: {}, Error Message: {}", ErrorCode.DATA_INTEGRITY_VIOLATION, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.DATA_INTEGRITY_VIOLATION, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
        
    } 

    // Handle generic exceptions
    @ExceptionHandler(Exception.class) 
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        logger.error("Error Code: {}, Error Message: {}", ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);

        ApiError apiError = new ApiError(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
}
