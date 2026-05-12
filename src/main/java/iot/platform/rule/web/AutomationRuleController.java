package iot.platform.rule.web;

import iot.platform.rule.service.AutomationRuleService;
import iot.platform.rule.web.dto.RuleRequest;
import iot.platform.rule.web.dto.RuleResponse;
import iot.platform.rule.web.dto.RuleUpdateRequest;
import iot.platform.security.SecurityUtils;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class AutomationRuleController {

    private final AutomationRuleService ruleService;

    @GetMapping
    public ResponseEntity<List<RuleResponse>> list() {
        return ResponseEntity.ok(ruleService.listForUser(SecurityUtils.currentUserId()));
    }

    @PostMapping
    public ResponseEntity<RuleResponse> create(@Valid @RequestBody RuleRequest request) {
        RuleResponse created = ruleService.create(SecurityUtils.currentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RuleResponse> update(@PathVariable UUID id, @Valid @RequestBody RuleUpdateRequest request) {
        return ResponseEntity.ok(ruleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        ruleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
