package dev.uniqueman.fullstack_app_spring_boot.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import dev.uniqueman.fullstack_app_spring_boot.Entity.Role;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;
import dev.uniqueman.fullstack_app_spring_boot.Entity.UserDTO;
import dev.uniqueman.fullstack_app_spring_boot.Exception.UsernameAlreadyExistsException;
import dev.uniqueman.fullstack_app_spring_boot.Repository.UserRepository;

@Service
public class UserService {
    
    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public boolean adminExists() {
        return userRepository.existsByRole(Role.ADMIN);
    }    

    public void registerAdmin(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username '" + username + "' is already taken.");
        }

        User admin = new User();

        String hashedPassword = passwordEncoder.encode(rawPassword);
        admin.setUsername(username);
        admin.setHashedPassword(hashedPassword);
        admin.setRole(Role.ADMIN);

        try {
            userRepository.save(admin);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("A data integrity error occurred while saving the admin.");
        }
        
    }

    public void registerUser(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String rawPassword = userDTO.getRawPassword();

        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username '" + username + "' is already taken");
        }

        User user = new User();
        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setUsername(username);
        user.setHashedPassword(hashedPassword);
        user.setRole(Role.USER);

        try {
            userRepository.save(user);

        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("A data integrity error occurred while saving the user");
        }
    }
}
