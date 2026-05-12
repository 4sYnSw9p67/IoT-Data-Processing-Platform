package iot.platform.exception;

import org.springframework.http.HttpStatus;

public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
