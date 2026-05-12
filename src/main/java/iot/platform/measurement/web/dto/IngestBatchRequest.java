package iot.platform.measurement.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record IngestBatchRequest(
        @NotEmpty
        @Size(max = 5000, message = "A batch may not exceed 5000 points")
        @Valid List<IngestPoint> points
) {
}
