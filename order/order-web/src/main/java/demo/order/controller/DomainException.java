package demo.order.controller;

import org.springframework.http.HttpStatus;

abstract class DomainException extends RuntimeException {

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

  @Override
  public String toString() {
    return "DomainException{" +
            "status=" + status +
            "} " + super.toString();
  }
}