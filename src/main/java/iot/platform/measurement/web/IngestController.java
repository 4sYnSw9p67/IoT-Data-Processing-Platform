package iot.platform.measurement.web;

import iot.platform.measurement.service.IngestionService;
import iot.platform.measurement.web.dto.IngestBatchRequest;
import iot.platform.measurement.web.dto.IngestResult;
import iot.platform.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestController {

    private final IngestionService ingestionService;

    @PostMapping(path = "/measurements", consumes = "application/json")
    public ResponseEntity<IngestResult> ingest(@Valid @RequestBody IngestBatchRequest request) {
        return ResponseEntity.ok(ingestionService.ingestBatch(SecurityUtils.currentUserId(), request));
    }
}
