package demo.domain;

public class ErrorResponse {
  private Long timestamp;
  private String path;
  private Integer status;
  private String error;
  private String message;
  private String traceId;

  public ErrorResponse() {
  }

  public ErrorResponse(Long timestamp, String path, Integer status, String error, String message, String traceId) {
    this.timestamp = timestamp;
    this.path = path;
    this.status = status;
    this.error = error;
    this.message = message;
    this.traceId = traceId;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }
}