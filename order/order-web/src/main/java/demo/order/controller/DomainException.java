package demo.order.controller;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString(callSuper = true)
abstract class DomainException extends RuntimeException {

  @Getter
  private final HttpStatus status;

  DomainException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  DomainException(HttpStatus status, String message, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}