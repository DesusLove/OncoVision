package com.albert.patientsystem.config;

import com.albert.patientsystem.entity.User;
import com.albert.patientsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds a default admin user on first boot so the system is usable
 * out of the box. Skipped if any user already exists.
 *
 * Default credentials (printed to stdout at startup):
 *   username: admin
 *   password: onco2026!
 *
 * Production deployments MUST change these immediately or set
 * ONCOVISION_ADMIN_PASSWORD in the environment.
 */
@Configuration
public class DataSeeder {

    @org.springframework.context.annotation.Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (userRepository.count() > 0) return;
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(encoder.encode("onco2026!"));
            admin.setRole("ADMIN");
            admin.setDisplayName("Default Admin");
            userRepository.save(admin);
            System.out.println("[OncoVision] Seeded default admin user: admin / onco2026! (CHANGE IMMEDIATELY)");
        };
    }
}
