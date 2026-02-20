package com.zest.productapi.config;

// ==========file-context==========

import com.zest.productapi.entity.Role;
import com.zest.productapi.entity.RoleName;
import com.zest.productapi.entity.User;
import com.zest.productapi.repository.RoleRepository;
import com.zest.productapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner bootstrapSecurityData(RoleRepository roleRepository,
                                                   UserRepository userRepository,
                                                   PasswordEncoder passwordEncoder,
                                                   @Value("${app.bootstrap.admin-email}") String adminEmail,
                                                   @Value("${app.bootstrap.admin-password}") String adminPassword,
                                                   @Value("${app.bootstrap.admin-name}") String adminName) {
        return args -> {
            // ==========base-roles==========
            Role roleUser = roleRepository.findByName(RoleName.ROLE_USER).orElseGet(() -> {
                Role role = new Role();
                role.setName(RoleName.ROLE_USER);
                return roleRepository.save(role);
            });

            Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseGet(() -> {
                Role role = new Role();
                role.setName(RoleName.ROLE_ADMIN);
                return roleRepository.save(role);
            });

            // ==========bootstrap-admin==========
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = new User();
                admin.setFullName(adminName);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setEnabled(true);
                admin.getRoles().add(roleUser);
                admin.getRoles().add(roleAdmin);
                userRepository.save(admin);
            }
        };
    }
}
