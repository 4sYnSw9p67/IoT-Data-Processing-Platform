package iot.platform.twin.web;

import iot.platform.security.SecurityUtils;
import iot.platform.twin.model.TwinType;
import iot.platform.twin.service.DigitalTwinService;
import iot.platform.twin.web.dto.TwinRequest;
import iot.platform.twin.web.dto.TwinResponse;
import iot.platform.twin.web.dto.TwinTreeNode;
import iot.platform.twin.web.dto.TwinTypeDescriptor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/twins")
@RequiredArgsConstructor
public class DigitalTwinController {

    private final DigitalTwinService twinService;

    @GetMapping
    public ResponseEntity<List<TwinResponse>> list(
            @RequestParam(value = "type", required = false) TwinType type,
            @RequestParam(value = "parentId", required = false) UUID parentId
    ) {
        return ResponseEntity.ok(twinService.listForUser(SecurityUtils.currentUserId(), type, parentId));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<TwinTreeNode>> tree() {
        return ResponseEntity.ok(twinService.tree(SecurityUtils.currentUserId()));
    }

    @GetMapping("/types")
    public ResponseEntity<List<TwinTypeDescriptor>> types() {
        return ResponseEntity.ok(Arrays.stream(TwinType.values()).map(TwinTypeDescriptor::of).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TwinResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(twinService.getById(id));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<TwinResponse>> children(@PathVariable UUID id) {
        return ResponseEntity.ok(twinService.children(id));
    }

    @PostMapping
    public ResponseEntity<TwinResponse> create(@Valid @RequestBody TwinRequest request) {
        TwinResponse created = twinService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TwinResponse> update(@PathVariable UUID id, @Valid @RequestBody TwinRequest request) {
        return ResponseEntity.ok(twinService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        twinService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
