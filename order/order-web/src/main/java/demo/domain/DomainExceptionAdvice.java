package demo.domain;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.util.UUID;

import static java.lang.System.currentTimeMillis;

@ControllerAdvice
class DomainExceptionAdvice {

    public DomainExceptionAdvice() {
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exc, ServerWebExchange exchange) {
        final var status = exc.getStatus();
        final var request = exchange.getRequest();

        return new ResponseEntity<>(new ErrorResponse(
                currentTimeMillis(),
                request.getPath().value(),
                status.value(),
                status.getReasonPhrase(),
                exc.getMessage(),
                UUID.randomUUID().toString()
        ), status);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException exc, ServerWebExchange exchange) {
        final var status = HttpStatus.BAD_REQUEST;
        final var request = exchange.getRequest();

        return new ResponseEntity<>(new ErrorResponse(
                currentTimeMillis(),
                request.getPath().value(),
                status.value(),
                status.getReasonPhrase(),
                exc.getMessage(),
                UUID.randomUUID().toString()
        ), status);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(WebExchangeBindException exc, ServerWebExchange exchange) {
        final var status = exc.getStatus();
        final var request = exchange.getRequest();

        return new ResponseEntity<>(new ErrorResponse(
                currentTimeMillis(),
                request.getPath().value(),
                status.value(),
                status.getReasonPhrase(),
                exc.getMessage(),
                UUID.randomUUID().toString()
        ), status);
    }

}