package iot.platform.measurement.web;

import iot.platform.measurement.service.IngestionService;
import iot.platform.measurement.web.dto.IngestResult;
import iot.platform.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class MeasurementBulkController {

    private final IngestionService ingestionService;

    @PostMapping(path = "/api/measurements/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<IngestResult> uploadCsv(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ingestionService.ingestCsv(SecurityUtils.currentUserId(), file));
    }
}
