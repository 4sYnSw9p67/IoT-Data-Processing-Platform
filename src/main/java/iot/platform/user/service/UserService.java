package iot.platform.user.service;

import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.user.model.Role;
import iot.platform.user.model.User;
import iot.platform.user.repository.UserRepository;
import iot.platform.user.web.dto.ProfileUpdateRequest;
import iot.platform.user.web.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request, Role role) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username '" + request.username() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email '" + request.email() + "' is already registered");
        }
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .role(role)
                .enabled(true)
                .build();
        User saved = userRepository.save(user);
        log.info("Registered new user id={} username={} role={}", saved.getId(), saved.getUsername(), saved.getRole());
        return saved;
    }

    @Transactional(readOnly = true)
    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateProfile(UUID id, ProfileUpdateRequest request) {
        User user = getById(id);
        if (StringUtils.hasText(request.firstName())) {
            user.setFirstName(request.firstName());
        }
        if (StringUtils.hasText(request.lastName())) {
            user.setLastName(request.lastName());
        }
        if (StringUtils.hasText(request.email()) && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException("Email '" + request.email() + "' is already registered");
            }
            user.setEmail(request.email());
        }
        log.info("Updated profile for user id={}", user.getId());
        return user;
    }

    @Transactional
    public User updateRole(UUID id, Role role) {
        User user = getById(id);
        if (user.getRole() == role) {
            return user;
        }
        log.info("Changing role for user id={} from {} to {}", user.getId(), user.getRole(), role);
        user.setRole(role);
        return user;
    }
}
