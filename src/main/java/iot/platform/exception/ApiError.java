package iot.platform.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> violations,
        Map<String, Object> details
) {

    @Builder
    public record FieldViolation(String field, String message) {
    }
}
