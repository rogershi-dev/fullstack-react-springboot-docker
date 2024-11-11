package dev.uniqueman.fullstack_app_spring_boot.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import dev.uniqueman.fullstack_app_spring_boot.Entity.Role;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;
import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Exception.UsernameAlreadyExistsException;
import dev.uniqueman.fullstack_app_spring_boot.Repository.UserRepository;

public class UserServiceTest {
    

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserDTO userDTO;
    private final String hashedPassword = "hashedPassword";

    @BeforeEach
    public void setup() {
 
        MockitoAnnotations.openMocks(this);
        userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setRawPassword("TestRawPassword1234");
    }

    @Test
    public void testAdminExists_WhenAdminExists() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        boolean result = userService.adminExists();
        assertTrue(result);
        verify(userRepository, times(1)).existsByRole(Role.ADMIN);
    }

    @Test
    public void testAdminExists_WhenAdminDoesNotExist() {
        when(userRepository.existsByRole(Role.ADMIN)).thenReturn(true);
        boolean result = userService.adminExists();
        assertTrue(result);
        verify(userRepository, times(1)).existsByRole(Role.ADMIN);
    }

    @Test
    public void testRegisterAdmin_Success() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(userDTO.getRawPassword())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(new User());
        userService.registerAdmin(userDTO);

        verify(userRepository, times(1)).existsByUsername(userDTO.getUsername());
        verify(passwordEncoder, times(1)).encode(userDTO.getRawPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterAdmin_UsernameAlreadyExists() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.registerAdmin(userDTO);
        });

        assertEquals("Username 'testuser' is already taken.", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(userDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    public void testRegisterAdmin_DataIntegrityViolationException() {

        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(userDTO.getRawPassword())).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Database error"));

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
            userService.registerAdmin(userDTO);
        } );

        assertEquals("A data integrity error occurred while saving the admin.", exception.getMessage());

        verify(userRepository, times(1)).existsByUsername(userDTO.getUsername());
        verify(passwordEncoder, times(1)).encode(userDTO.getRawPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUser_Success() {

        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(new User());

        userService.registerUser(userDTO);

        verify(userRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));

    }
    
    @Test
    public void testRegisterUser_UsernameAlreadyExists() {

        String username = userDTO.getUsername();
        when(userRepository.existsByUsername(username)).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.registerUser(userDTO);
        });

        assertEquals("Username 'testuser' is already taken", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_DataIntegrityViolationException() {

        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class)))
            .thenThrow(new DataIntegrityViolationException("A data integrity error occurred while saving the user"));

        DataIntegrityViolationException exception = assertThrows(DataIntegrityViolationException.class, () -> {
            userService.registerUser(userDTO);
        });

        assertEquals("A data integrity error occurred while saving the user", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }
}
