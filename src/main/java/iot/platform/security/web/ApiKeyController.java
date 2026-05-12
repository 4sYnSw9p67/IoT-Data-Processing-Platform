package iot.platform.security.web;

import iot.platform.security.SecurityUtils;
import iot.platform.security.model.ApiKey;
import iot.platform.security.service.ApiKeyService;
import iot.platform.security.web.dto.ApiKeyCreateRequest;
import iot.platform.security.web.dto.ApiKeyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list() {
        List<ApiKeyResponse> response = apiKeyService.listForUser(SecurityUtils.currentUserId()).stream()
                .map(this::toRedactedResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> issue(@Valid @RequestBody ApiKeyCreateRequest request) {
        ApiKeyService.Issued issued = apiKeyService.issue(SecurityUtils.currentUserId(), request.label());
        ApiKey entity = issued.entity();
        ApiKeyResponse response = ApiKeyResponse.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .secret(issued.secretToken())
                .createdAt(entity.getCreatedAt())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id) {
        apiKeyService.revoke(id);
        return ResponseEntity.noContent().build();
    }

    private ApiKeyResponse toRedactedResponse(ApiKey entity) {
        return ApiKeyResponse.builder()
                .id(entity.getId())
                .label(entity.getLabel())
                .lastUsedAt(entity.getLastUsedAt())
                .revokedAt(entity.getRevokedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
