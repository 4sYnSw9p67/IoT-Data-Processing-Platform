package iot.platform.user.web;

import iot.platform.user.service.UserService;
import iot.platform.user.web.dto.ProfileResponse;
import iot.platform.user.web.dto.UpdateRoleRequest;
import iot.platform.user.web.dto.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProfileResponse>> listUsers() {
        List<ProfileResponse> users = userService.listAll().stream()
                .map(UserMapper::toProfile)
                .toList();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<ProfileResponse> updateRole(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(UserMapper.toProfile(userService.updateRole(id, request.role())));
    }
}
