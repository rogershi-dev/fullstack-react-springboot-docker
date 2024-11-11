package dev.uniqueman.fullstack_app_spring_boot.Controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.uniqueman.fullstack_app_spring_boot.Component.JwtUtilization;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;
import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Exception.JwtAuthenticationException;
import dev.uniqueman.fullstack_app_spring_boot.Repository.UserRepository;
import dev.uniqueman.fullstack_app_spring_boot.Service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtilization jwtUtilization;

    @Autowired
    TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Value("${app.device-limit}")
    private int deviceLimit;


    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody UserDTO userDTO) {
        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, rawPassword)
        );

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with the username: " + username));

        String token = jwtUtilization.generateToken(user);
        Duration tokenRemainingDuration = jwtUtilization.getTokenRemainingDuration(token);
        tokenService.addToken(username, token, deviceLimit, tokenRemainingDuration);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        
        String token = (String) request.getAttribute("JWT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new JwtAuthenticationException("Token not found in the request header");
        }

        String username = jwtUtilization.getUsernameFromToken(token);
        if (username == null || username.isEmpty()) {
            throw new JwtAuthenticationException("Token invalid");
        }

        tokenService.removeToken(username, token);
        Duration tokenRemainingDuration = jwtUtilization.getTokenRemainingDuration(token);
        tokenService.addToBlacklist(token, tokenRemainingDuration);
        return ResponseEntity.status(HttpStatus.OK).body("Log out successfully");

    }

    @GetMapping("/verify-token")
    public ResponseEntity<Object> verifyToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new JwtAuthenticationException("Token is invalid or expired.");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Token verified successfully");
    }
}
