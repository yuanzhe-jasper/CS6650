public class RecordInfo {

  private Long startTime;
  private String requestType;
  private Long latency;
  private Integer statusCode;

  public RecordInfo(Long startTime, String requestType, Long latency, Integer statusCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.statusCode = statusCode;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public String getRequestType() {
    return requestType;
  }

  public void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public Long getLatency() {
    return latency;
  }

  public void setLatency(Long latency) {
    this.latency = latency;
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setResponseCode(Integer statusCode) {
    this.statusCode = statusCode;
  }
}
