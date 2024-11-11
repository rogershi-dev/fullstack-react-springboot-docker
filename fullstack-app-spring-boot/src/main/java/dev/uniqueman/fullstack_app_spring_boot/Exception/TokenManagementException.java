package dev.uniqueman.fullstack_app_spring_boot.Exception;

public class TokenManagementException extends RuntimeException {
    public TokenManagementException (String errorMessage) {
        super(errorMessage);
    }
}
