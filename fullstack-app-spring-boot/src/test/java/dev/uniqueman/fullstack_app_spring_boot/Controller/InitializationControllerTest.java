package dev.uniqueman.fullstack_app_spring_boot.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.uniqueman.fullstack_app_spring_boot.Component.InitializationStatus;
import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Exception.UsernameAlreadyExistsException;
import dev.uniqueman.fullstack_app_spring_boot.Service.UserService;

@SpringBootTest
@AutoConfigureMockMvc 
public class InitializationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean 
    private InitializationStatus initializationStatus;

    @MockBean
    private UserService userService;

    private UserDTO userDTO;

    @BeforeEach
    public void setup() {
        userDTO = new UserDTO();
        userDTO.setUsername("adminaccount");
        userDTO.setRawPassword("AdminAccountRawPassowrd");
    }

    @Test
    public void testGetInitializationStatus_NeedsInitialization() throws Exception {
        when(initializationStatus.needsAdminInitialization()).thenReturn(true);

        mockMvc.perform(get("/application-setup/get-initialization-status"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"needsAdminInitialization\":true}"));

        verify(initializationStatus, times(1)).needsAdminInitialization();
    }

    @Test
    public void testGetInitializationStatus_DonNotNeedInitialization() throws Exception {
        when(initializationStatus.needsAdminInitialization()).thenReturn(false);

        mockMvc.perform(get("/application-setup/get-initialization-status"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"needsAdminInitialization\":false}"));

        verify(initializationStatus, times(1)).needsAdminInitialization();
    }

    @Test
    public void testInitializeAdmin_Success() throws Exception {
        when(initializationStatus.needsAdminInitialization()).thenReturn(true);

        doNothing().when(userService).registerAdmin(any(UserDTO.class));
        doNothing().when(initializationStatus).setNeedsAdminInitialization(false);

        mockMvc.perform(post("/application-setup/initialize-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"adminaccount\",\"rawPassword\":\"AdminAccountRawPassowrd\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("Admin initialized successfully"));
        
        verify(initializationStatus, times(1)).needsAdminInitialization();
        verify(userService, times(1)).registerAdmin(any(UserDTO.class));
        verify(initializationStatus, times(1)).setNeedsAdminInitialization(false);
    }

    @ParameterizedTest
    @CsvSource({
        // username edge cases
        "'', 'ValidRawPassword'", 
        "'ab', 'ValidRawPassword'", 
        "'a_very_long_username_exceeding_fifty_characters_1234567890', 'ValidRawPassword'", 
        "'user!name', 'ValidRawPassword'", 
        // rawPassword edge cases
        "'ValidUsername', ''", 
        "'ValidUsername', 'short'", 
        "'ValidUsername', 'a_very_long_raw_password_exceeding_fifty_characters_1234567890'", 
        "'ValidUsername', 'pass@word)'"
    })
    public void testInitializeAdmin_InValidUserDTO(String username, String rawPassword) throws Exception {

        String requestBody = String.format("{\"username\":\"%s\",\"rawPassword\":\"%s\"}", username, rawPassword);

        mockMvc.perform(post("/application-setup/initialize-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("FIELD_VALIDATION_FAILED"));
            

        verify(initializationStatus, never()).needsAdminInitialization();
        verify(userService, never()).registerAdmin(any(UserDTO.class));
        verify(initializationStatus, never()).setNeedsAdminInitialization(anyBoolean());
    }

    @Test
    public void testInitializeAdmin_AdminAlreadyInitialized() throws Exception {
    
        when(initializationStatus.needsAdminInitialization()).thenReturn(false);

        mockMvc.perform(post("/application-setup/initialize-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"adminaccount\",\"rawPassword\":\"AdminAccountRawPassowrd\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("ADMIN_ALREADY_INITIALIZED"))
            .andExpect(jsonPath("$.message").value("Too many attempts, admin initialized already"));

        verify(initializationStatus, times(1)).needsAdminInitialization();
        verify(userService, never()).registerAdmin(any(UserDTO.class));
        verify(initializationStatus, never()).setNeedsAdminInitialization(anyBoolean());
    }

    @Test
    public void testInitializeAdmin_UsernameAlreadyExists() throws Exception {
        when(initializationStatus.needsAdminInitialization()).thenReturn(true);

        doThrow(new UsernameAlreadyExistsException("Username '" + userDTO.getUsername() + "' is already taken."))
            .when(userService).registerAdmin(any(UserDTO.class));

        mockMvc.perform(post("/application-setup/initialize-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"adminaccount\",\"rawPassword\":\"AdminAccountRawPassowrd\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"));     
        
        verify(initializationStatus, times(1)).needsAdminInitialization();
        verify(userService, times(1)).registerAdmin(any(UserDTO.class));
        verify(initializationStatus, never()).setNeedsAdminInitialization(anyBoolean());   
    }

    @Test
    public void testInitializeAdmin_DataIntegrityViolated() throws Exception {

        when(initializationStatus.needsAdminInitialization()).thenReturn(true);

        doThrow(new DataIntegrityViolationException("A data integrity error occurred."))
            .when(userService).registerAdmin(any(UserDTO.class));
        
        mockMvc.perform(post("/application-setup/initialize-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"adminaccount\",\"rawPassword\":\"AdminAccountRawPassowrd\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("DATA_INTEGRITY_VIOLATION"));

        verify(initializationStatus, times(1)).needsAdminInitialization();
        verify(userService, times(1)).registerAdmin(any(UserDTO.class));
        verify(initializationStatus, never()).setNeedsAdminInitialization(anyBoolean());   
    }

    
}
