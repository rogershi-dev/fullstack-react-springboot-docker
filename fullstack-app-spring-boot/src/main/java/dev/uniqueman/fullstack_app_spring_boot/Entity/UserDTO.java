package dev.uniqueman.fullstack_app_spring_boot.Entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserDTO {

    @NotBlank(message = "Username is mandatory")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters long")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "Username can only contain letters and digits"
    )
    private String username;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters long")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$",
        message = "Password can only contain letters and digits"
    )
    private String rawPassword;


    public void setUsername(String username) {
        this.username = username;
    }

    public void setRawPassword(String rawPassword) {
        this.rawPassword = rawPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getRawPassword() {
        return rawPassword;
    }

}
