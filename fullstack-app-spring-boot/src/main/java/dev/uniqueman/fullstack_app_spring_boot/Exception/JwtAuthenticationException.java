package dev.uniqueman.fullstack_app_spring_boot.Exception;

public class JwtAuthenticationException extends RuntimeException{
    public JwtAuthenticationException(String errorMessage) {
        super(errorMessage);
    }
}
