package iot.platform.user.web;

import iot.platform.security.SecurityUtils;
import iot.platform.user.service.UserService;
import iot.platform.user.web.dto.ProfileResponse;
import iot.platform.user.web.dto.ProfileUpdateRequest;
import iot.platform.user.web.dto.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ProfileResponse> me() {
        return ResponseEntity.ok(UserMapper.toProfile(userService.getById(SecurityUtils.currentUserId())));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> update(@Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(UserMapper.toProfile(userService.updateProfile(SecurityUtils.currentUserId(), request)));
    }
}
