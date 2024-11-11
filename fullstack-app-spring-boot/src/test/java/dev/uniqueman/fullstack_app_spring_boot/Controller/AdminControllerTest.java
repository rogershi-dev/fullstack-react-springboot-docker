package dev.uniqueman.fullstack_app_spring_boot.Controller;

import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.uniqueman.fullstack_app_spring_boot.Component.JwtUtilization;
import dev.uniqueman.fullstack_app_spring_boot.Entity.Role;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;
import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Repository.UserRepository;
import dev.uniqueman.fullstack_app_spring_boot.Service.CustomUserDetailsService;
import dev.uniqueman.fullstack_app_spring_boot.Service.TokenService;
import dev.uniqueman.fullstack_app_spring_boot.Service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {
    
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtilization jwtUtilization;

    @MockBean
    private TokenService tokenService;

    @MockBean 
    private AuthenticationManager authenticationManager;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private UserDTO userDTO;
    private User user;
    private String token;
    private String hashedPassword;

    @BeforeEach
    public void setUp() {
        hashedPassword = "IAmHashedPassword";
        token = "IAmValidToken";

        userDTO = new UserDTO();
        userDTO.setUsername("IAmUsername");
        userDTO.setRawPassword("IAmRawPassword");

        user = new User();
        user.setId(2L);
        user.setUsername(userDTO.getRawPassword());
        user.setHashedPassword(hashedPassword);
        user.setRole(Role.ADMIN);


    }   

    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Test
    public void testAddNewUser_Success() throws Exception {
        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();

        when(jwtUtilization.validateToken(token)).thenReturn(true);
        when(jwtUtilization.getUsernameFromToken(token)).thenReturn(username);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);
        when(tokenService.isTokenActive(username, token)).thenReturn(true);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getHashedPassword(),
            getAuthorities(user.getRole())
        );

        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);
        doNothing().when(userService).registerUser(any(UserDTO.class));

        String requestBody = String.format("{\"username\":\"%s\",\"rawPassword\":\"%s\"}", username, rawPassword);
        mockMvc.perform(post("/admin/add-new-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("User added successfully"));

        verify(jwtUtilization, times(1)).validateToken(token);
        verify(jwtUtilization, times(1)).getUsernameFromToken(token);
        verify(tokenService, times(1)).isTokenBlacklisted(token);
        verify(tokenService, times(1)).isTokenActive(username, token);
        verify(customUserDetailsService, times(1)).loadUserByUsername(username);
        verify(userService, times(1)).registerUser(any(UserDTO.class));
    }

    @Test
    public void testAddNewUser_JWTValidationFailed() throws Exception {

        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();
        when(jwtUtilization.validateToken(token)).thenReturn(false);

        String requestBody = String.format("{\"username\":\"%s\",\"rawPassword\":\"%s\"}", username, rawPassword);
        mockMvc.perform(post("/admin/add-new-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("JWT_AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(jwtUtilization, times(1)).validateToken(token);
        verify(jwtUtilization, never()).getUsernameFromToken(anyString());
        verify(tokenService, never()).isTokenBlacklisted(anyString());
        verify(tokenService, never()).isTokenActive(anyString(), anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());
        verify(userService, never()).registerUser(any(UserDTO.class));

    }

    @ParameterizedTest
    @CsvSource({
        // username edge cases
        "'', 'IAmValidRawPassword'",
        "'u', 'IAmValidRawPassword'",
        "'I_am_a_very_long_username_that_exceeds_fifty_characters', 'IAmValidRawPassword'",
        "'usernameWith@', 'IAmValidRawPassword'",

        // rawPassword edge cases
        "'IAmValidUsername', ''",
        "'IAmValidUsername', 'c'",
        "'IAmValidUsername', 'I_am_a_very_long_raw_password_that_exceeds_fifty_characters'",
        "'IAmValidUsername', 'rawPasswordWith$'",
    })
    public void testAddNewUser_InvalidUserDTO(String username, String rawPassword) throws Exception {
        when(jwtUtilization.validateToken(token)).thenReturn(true);
        when(jwtUtilization.getUsernameFromToken(token)).thenReturn(username);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);
        when(tokenService.isTokenActive(username, token)).thenReturn(true);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getHashedPassword(),
            getAuthorities(user.getRole())
        );

        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);
        

        String requestBody = String.format("{\"username\":\"%s\",\"rawPassword\":\"%s\"}", username, rawPassword);
        mockMvc.perform(post("/admin/add-new-user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("FIELD_VALIDATION_FAILED"));

        verify(jwtUtilization, times(1)).validateToken(anyString());
        verify(jwtUtilization, times(1)).getUsernameFromToken(anyString());
        verify(tokenService, times(1)).isTokenBlacklisted(anyString());
        verify(tokenService, times(1)).isTokenActive(anyString(), anyString());
        verify(customUserDetailsService, times(1)).loadUserByUsername(anyString());
        verify(userService, never()).registerUser(any(UserDTO.class));
    }

    @Test
    public void testVerifyToken_Success() throws Exception {
        String username = userDTO.getUsername();

        when(jwtUtilization.validateToken(token)).thenReturn(true);
        when(jwtUtilization.getUsernameFromToken(token)).thenReturn(username);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);
        when(tokenService.isTokenActive(username, token)).thenReturn(true);

        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getHashedPassword(),
            getAuthorities(user.getRole())
        );

        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);

        mockMvc.perform(get("/auth/verify-token")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(content().string("Token verified successfully"));
    }

    @Test
    public void testVerifyToken_InvalidOrExpiredToken() throws Exception {
        when(jwtUtilization.validateToken(token)).thenReturn(false);

        mockMvc.perform(get("/auth/verify-token")
        .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("JWT_AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
