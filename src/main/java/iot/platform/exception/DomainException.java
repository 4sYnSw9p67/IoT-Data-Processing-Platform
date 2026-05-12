package iot.platform.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;

    protected DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    protected DomainException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
