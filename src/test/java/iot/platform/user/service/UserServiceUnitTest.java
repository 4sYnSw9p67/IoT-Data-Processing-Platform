package iot.platform.user.service;

import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.user.model.Role;
import iot.platform.user.model.User;
import iot.platform.user.repository.UserRepository;
import iot.platform.user.web.dto.ProfileUpdateRequest;
import iot.platform.user.web.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest("john", "john@example.com", "secret123", "John", "Doe");
    }

    @Test
    void register_storesHashedPassword() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(validRequest, Role.USER);

        assertThat(saved.getUsername()).isEqualTo("john");
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void register_rejectsDuplicateUsername() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validRequest, Role.USER))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(validRequest, Role.USER))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void getById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getById(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByUsername_notFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getByUsername("ghost")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateProfile_updatesEmailAndNames() {
        UUID id = UUID.randomUUID();
        User existing = User.builder()
                .id(id)
                .username("john")
                .email("old@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .enabled(true)
                .firstName("Old")
                .lastName("Name")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        User updated = userService.updateProfile(
                id,
                new ProfileUpdateRequest("Jane", "Smith", "new@example.com"));

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Smith");
    }

    @Test
    void updateProfile_emailConflict_throws() {
        UUID id = UUID.randomUUID();
        User existing = User.builder()
                .id(id)
                .username("john")
                .email("old@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateProfile(
                id,
                new ProfileUpdateRequest(null, null, "taken@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateRole_changesRole() {
        UUID id = UUID.randomUUID();
        User existing = User.builder()
                .id(id)
                .username("john")
                .email("john@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateRole(id, Role.ADMIN);

        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void updateRole_sameRole_isNoOp() {
        UUID id = UUID.randomUUID();
        User existing = User.builder()
                .id(id)
                .username("john")
                .email("john@example.com")
                .passwordHash("hash")
                .role(Role.USER)
                .enabled(true)
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        User updated = userService.updateRole(id, Role.USER);

        assertThat(updated.getRole()).isEqualTo(Role.USER);
    }
}
