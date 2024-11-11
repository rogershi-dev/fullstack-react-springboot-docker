package dev.uniqueman.fullstack_app_spring_boot.Component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import dev.uniqueman.fullstack_app_spring_boot.Service.UserService;

public class InitializationStatusTest {
    
    @Mock
    private UserService userService;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @InjectMocks
    private InitializationStatus initializationStatus;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNeedsAdminInitialization_AdminExists() {
        when(userService.adminExists()).thenReturn(true);

        initializationStatus.onApplicationEvent(applicationReadyEvent);

        assertFalse(initializationStatus.needsAdminInitialization(), "needsAdminInitialization should be false when admin presents");
        verify(userService, times(1)).adminExists();
    }

    @Test
    public void testNeedsAdminInitialization_AdminDoesNotExist() {

        when(userService.adminExists()).thenReturn(false);

        initializationStatus.onApplicationEvent(applicationReadyEvent);
        assertTrue(initializationStatus.needsAdminInitialization(), "needsAdminInitialization should be true when admin does not present");
        verify(userService, times(1)).adminExists();
    }

    @Test
    public void testSetNeedsAdminInitialization() {

        initializationStatus.setNeedsAdminInitialization(true);
        assertTrue(initializationStatus.needsAdminInitialization(),
            "Getter should return the value 'true' set by the setter");

        initializationStatus.setNeedsAdminInitialization(false);
        assertFalse(initializationStatus.needsAdminInitialization(),
            "Getter should return the value 'false' set by the setter");
    }
}
