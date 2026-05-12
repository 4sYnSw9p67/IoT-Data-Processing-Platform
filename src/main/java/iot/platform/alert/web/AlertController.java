package iot.platform.alert.web;

import iot.platform.alert.service.AlertService;
import iot.platform.alert.web.dto.AlertResponse;
import iot.platform.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> list(@RequestParam(defaultValue = "false") boolean unacknowledgedOnly) {
        return ResponseEntity.ok(alertService.listForUser(SecurityUtils.currentUserId(), unacknowledgedOnly));
    }

    @PutMapping("/{id}/ack")
    public ResponseEntity<AlertResponse> acknowledge(@PathVariable UUID id) {
        return ResponseEntity.ok(alertService.acknowledge(id));
    }
}
