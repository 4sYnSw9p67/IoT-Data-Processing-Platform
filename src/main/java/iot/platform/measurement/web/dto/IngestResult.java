package iot.platform.measurement.web.dto;

import java.util.List;

public record IngestResult(int accepted, List<Failure> failures) {

    public record Failure(int index, String reason) {
    }
}
