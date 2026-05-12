package iot.platform.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
