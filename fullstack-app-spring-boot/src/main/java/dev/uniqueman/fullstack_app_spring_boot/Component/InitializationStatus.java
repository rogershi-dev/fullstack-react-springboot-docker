package dev.uniqueman.fullstack_app_spring_boot.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import dev.uniqueman.fullstack_app_spring_boot.Service.UserService;


@Component
public class InitializationStatus implements ApplicationListener<ApplicationReadyEvent>{
    
    private boolean needsAdminInitialization;

    private final UserService userService;
    
    @Autowired
    public InitializationStatus(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        checkInitializationStatus();
    }

    private void checkInitializationStatus() {
        needsAdminInitialization = !userService.adminExists();
    }

    public boolean needsAdminInitialization() {
        return needsAdminInitialization;
    }

    public void setNeedsAdminInitialization(boolean value) {
        this.needsAdminInitialization = value;
    }
}
