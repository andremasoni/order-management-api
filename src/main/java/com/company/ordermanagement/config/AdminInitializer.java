package com.company.ordermanagement.config;

import com.company.ordermanagement.entity.AppUser;
import com.company.ordermanagement.entity.Role;
import com.company.ordermanagement.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String adminUsername;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            log.info("Admin bootstrap disabled: app.admin.username or app.admin.password is empty");
            return;
        }

        if (appUserRepository.existsByUsername(adminUsername)) {
            log.info("Admin user '{}' already exists", adminUsername);
            return;
        }

        AppUser admin = new AppUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ROLE_ADMIN);
        appUserRepository.save(admin);

        log.info("Admin user '{}' created", adminUsername);
    }
}
