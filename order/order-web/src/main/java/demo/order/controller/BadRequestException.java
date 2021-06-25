package demo.order.controller;

import org.springframework.http.HttpStatus;

public class BadRequestException extends DomainException {
    public BadRequestException(HttpStatus status, String message) {
        super(status, message);
    }

    public BadRequestException(HttpStatus status, String message, Throwable cause) {
        super(status, message, cause);
    }
}
