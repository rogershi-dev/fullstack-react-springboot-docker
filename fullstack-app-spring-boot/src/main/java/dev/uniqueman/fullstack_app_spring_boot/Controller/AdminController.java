package dev.uniqueman.fullstack_app_spring_boot.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    UserService userService;

    @PostMapping("/add-new-user")
    public ResponseEntity<Object> addNewUser(@Valid @RequestBody UserDTO userDTO) {
        
        userService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.OK).body("User added successfully");
        
    }
}
