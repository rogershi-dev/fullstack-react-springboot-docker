package dev.uniqueman.fullstack_app_spring_boot.Controller;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import dev.uniqueman.fullstack_app_spring_boot.Exception.TokenManagementException;
import dev.uniqueman.fullstack_app_spring_boot.Repository.UserRepository;
import dev.uniqueman.fullstack_app_spring_boot.Service.CustomUserDetailsService;
import dev.uniqueman.fullstack_app_spring_boot.Service.TokenService;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    
    @Autowired
    MockMvc mockMvc;

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

    private int deviceLimit;
    private UserDTO validUserDTO;
    private User validUser;
    private String validToken;

    @BeforeEach
    public void setUp() {
        validUserDTO = new UserDTO();
        validUserDTO.setUsername("validUsername");
        validUserDTO.setRawPassword("validRawPassword");

        validUser = new User();
        validUser.setId(123456789L);
        validUser.setUsername(validUserDTO.getUsername());
        validUser.setHashedPassword("hashedPassword");
        validUser.setRole(Role.ADMIN);

        deviceLimit = 2;
        validToken = "IAmTheGeneratedValidToken";
    }


    @Test
    public void testLogin_Success() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);

        when(userRepository.findByUsername(validUserDTO.getUsername())).thenReturn(Optional.of(validUser));
        when(jwtUtilization.generateToken(validUser)).thenReturn(validToken);
        when(jwtUtilization.getTokenRemainingDuration(validToken)).thenReturn(Duration.ofHours(1));
        doNothing().when(tokenService).addToken(eq(validUserDTO.getUsername()), eq(validToken), eq(deviceLimit), any(Duration.class));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"validUsername\",\"rawPassword\":\"validRawPassword\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value(validToken))
            .andExpect(jsonPath("$.username").value(validUserDTO.getUsername()))
            .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByUsername(validUserDTO.getUsername());
        verify(jwtUtilization, times(1)).generateToken(validUser);
        verify(jwtUtilization, times(1)).getTokenRemainingDuration(validToken);
        verify(tokenService, times(1))
            .addToken(eq(validUserDTO.getUsername()), eq(validToken), eq(deviceLimit), any(Duration.class));

    }

    @Test
    public void testLogin_AuthenticationFailed() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"validUsername\",\"rawPassword\":\"validRawPassword\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("USER_AUTHENTICATION_FAILED"));

        verify(authenticationManager, times(1))
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtilization, never()).generateToken(any(User.class));
        verify(jwtUtilization, never()).getTokenRemainingDuration(anyString());
        verify(tokenService, never()).addToken(anyString(), anyString(), anyInt(), any(Duration.class));
        
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
    public void testLogin_InvalidUserDTO(String username, String rawPassword) throws Exception {
        String requestBody = String.format("{\"username\":\"%s\",\"rawPassword\":\"%s\"}", username, rawPassword);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("FIELD_VALIDATION_FAILED"));

        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtilization, never()).generateToken(any(User.class));
        verify(jwtUtilization, never()).getTokenRemainingDuration(anyString());
        verify(tokenService, never()).addToken(anyString(), anyString(), anyInt(), any(Duration.class));
    }


    private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }


    @Test
    public void testLogout_Success() throws Exception{
        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
            validUser.getUsername(),
            validUser.getHashedPassword(),
            getAuthorities(validUser.getRole())
        );

        String username = validUserDTO.getUsername();
        Duration duration = Duration.ofHours(1);

        when(jwtUtilization.validateToken(validToken)).thenReturn(true);
        when(jwtUtilization.getUsernameFromToken(validToken)).thenReturn(username);
        when(tokenService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(tokenService.isTokenActive(username, validToken)).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(validUser.getUsername())).thenReturn(mockUserDetails);

        doNothing().when(tokenService).removeToken(username, validToken);
        when(jwtUtilization.getTokenRemainingDuration(validToken)).thenReturn(duration);
        doNothing().when(tokenService).addToBlacklist(validToken, duration);

        mockMvc.perform(get("/auth/logout")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isOk())
            .andExpect(content().string("Log out successfully"));

        verify(jwtUtilization, times(1)).validateToken(validToken);
        verify(jwtUtilization, times(2)).getUsernameFromToken(validToken);
        verify(tokenService, times(1)).isTokenBlacklisted(validToken);
        verify(tokenService, times(1)).isTokenActive(username, validToken);
        verify(customUserDetailsService, times(1)).loadUserByUsername(username);

        verify(tokenService, times(1)).removeToken(username, validToken);
        verify(jwtUtilization, times(1)).getTokenRemainingDuration(validToken);
        verify(tokenService, times(1)).addToBlacklist(validToken, duration);
        
        
    }

    @Test
    public void testLogout_TokenDoesNotExist() throws Exception {
        mockMvc.perform(get("/auth/logout"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("JWT_AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Token is missing"));
        
        verify(jwtUtilization, never()).validateToken(anyString());
        verify(jwtUtilization, never()).getUsernameFromToken(anyString());
        verify(tokenService, never()).isTokenBlacklisted(anyString());
        verify(tokenService, never()).isTokenActive(anyString(), anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        verify(tokenService, never()).removeToken(anyString(), anyString());
        verify(jwtUtilization, never()).getTokenRemainingDuration(anyString());
        verify(tokenService, never()).addToBlacklist(anyString(), any(Duration.class));
    }

    @Test
    public void testLogout_TokenValidationFailed() throws Exception {
  
        String invalidToken = "InvalidTokenIsMe";

        when(jwtUtilization.validateToken(invalidToken)).thenReturn(false);

        mockMvc.perform(get("/auth/logout")
                .header("Authorization", "Bearer " + invalidToken))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("JWT_AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verify(jwtUtilization, times(1)).validateToken(invalidToken);
        verify(jwtUtilization, never()).getUsernameFromToken(anyString());
        verify(tokenService, never()).isTokenBlacklisted(anyString());
        verify(tokenService, never()).isTokenActive(anyString(), anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        verify(tokenService, never()).removeToken(anyString(), anyString());
        verify(jwtUtilization, never()).getTokenRemainingDuration(anyString());
        verify(tokenService, never()).addToBlacklist(anyString(), any(Duration.class));

    }

    @Test
    public void testLogout_TokenBlacklisted() throws Exception {
       
        when(jwtUtilization.validateToken(validToken)).thenReturn(true);
        when(jwtUtilization.getUsernameFromToken(validToken)).thenReturn(validUserDTO.getUsername());
        when(tokenService.isTokenBlacklisted(validToken)).thenReturn(true);

        mockMvc.perform(get("/auth/logout")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("JWT_AUTHENTICATION_FAILED"))
            .andExpect(jsonPath("$.message").value("Token has been blacklisted"));

        verify(jwtUtilization, times(1)).validateToken(validToken);
        verify(jwtUtilization, times(1)).getUsernameFromToken(validToken);
        verify(tokenService, times(1)).isTokenBlacklisted(validToken);
        verify(tokenService, never()).isTokenActive(anyString(), anyString());
        verify(customUserDetailsService, never()).loadUserByUsername(anyString());

        verify(tokenService, never()).removeToken(anyString(), anyString());
        verify(jwtUtilization, never()).getTokenRemainingDuration(anyString());
        verify(tokenService, never()).addToBlacklist(anyString(), any(Duration.class));

    }

    @Test
    public void testLogout_TokenRemovalError() throws Exception {
    
        String username = validUserDTO.getUsername();
        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(
            validUser.getUsername(),
            validUser.getHashedPassword(),
            getAuthorities(validUser.getRole())
        );

        when(jwtUtilization.validateToken(validToken)).thenReturn(true);
        when(jwtUtilization.getUsernameFromToken(validToken)).thenReturn(username);
        when(tokenService.isTokenBlacklisted(validToken)).thenReturn(false);
        when(tokenService.isTokenActive(username, validToken)).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(username)).thenReturn(mockUserDetails);

        doThrow(new TokenManagementException("Token removal failed")).when(tokenService).removeToken(anyString(), anyString());

        mockMvc.perform(get("/auth/logout")
                .header("Authorization", "Bearer " + validToken))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.error").value("TOKEN_MANAGEMENT_ERROR"))
            .andExpect(jsonPath("$.message").value("Token removal failed"));

        verify(jwtUtilization, times(1)).validateToken(validToken);
        verify(jwtUtilization, times(2)).getUsernameFromToken(validToken);
        verify(tokenService, times(1)).isTokenBlacklisted(validToken);
        verify(tokenService, times(1)).isTokenActive(username, validToken);
        verify(customUserDetailsService, times(1)).loadUserByUsername(username);

        verify(tokenService, times(1)).removeToken(username, validToken);
        
        verify(jwtUtilization, never()).getTokenRemainingDuration(anyString());
        verify(tokenService, never()).addToBlacklist(anyString(), any(Duration.class));
    }
}
