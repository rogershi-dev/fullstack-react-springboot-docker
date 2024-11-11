package dev.uniqueman.fullstack_app_spring_boot.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.uniqueman.fullstack_app_spring_boot.Component.InitializationStatus;
import dev.uniqueman.fullstack_app_spring_boot.Entity.ApiError;
import dev.uniqueman.fullstack_app_spring_boot.Entity.ErrorCode;
import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/application-setup")
public class InitializationController {

    @Autowired
    InitializationStatus initializationStatus;

    @Autowired
    UserService userService;


    @GetMapping("/get-initialization-status")
    public ResponseEntity<Object> getInitializationStatus() {
        Map<String, Boolean> status = new HashMap<>();
        status.put("needsAdminInitialization", initializationStatus.needsAdminInitialization());

        return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    @PostMapping("/initialize-admin")
    public ResponseEntity<Object> initializeAdmin(@Valid @RequestBody UserDTO userDTO) {
        if (!initializationStatus.needsAdminInitialization()) {
            ApiError apiError = new ApiError(ErrorCode.ADMIN_ALREADY_INITIALIZED, "Too many attempts, admin initialized already");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
        }   

        userService.registerAdmin(userDTO);

        initializationStatus.setNeedsAdminInitialization(false);
        return ResponseEntity.status(HttpStatus.OK).body("Admin initialized successfully");
    }
}
