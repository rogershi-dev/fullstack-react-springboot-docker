package dev.uniqueman.fullstack_app_spring_boot.Entity;

import java.time.LocalDateTime;

public class ApiError {
    private ErrorCode error;
    private String message;
    private LocalDateTime timestamp;

    public ApiError() {
        
    }

    public ApiError(ErrorCode error, String message) {
        this.timestamp = LocalDateTime.now();
        this.error = error;
        this.message = message;
    }

    public ErrorCode getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setError(ErrorCode error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
