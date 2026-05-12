package iot.platform.user.service;

import iot.platform.user.model.Role;
import iot.platform.user.model.User;
import iot.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin-email:admin@iot.local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin-password:admin12345}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName("Platform")
                .lastName("Administrator")
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(admin);
        log.warn("Bootstrap admin user created: username={} (password from app.bootstrap.admin-password)", adminUsername);
    }
}
