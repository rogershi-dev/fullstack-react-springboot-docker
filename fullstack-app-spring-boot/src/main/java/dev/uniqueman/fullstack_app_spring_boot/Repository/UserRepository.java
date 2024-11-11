package dev.uniqueman.fullstack_app_spring_boot.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.uniqueman.fullstack_app_spring_boot.Entity.Role;
import dev.uniqueman.fullstack_app_spring_boot.Entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByUsername(String username);
    boolean existsByRole(Role role);
    boolean existsByUsername(String username);
}
