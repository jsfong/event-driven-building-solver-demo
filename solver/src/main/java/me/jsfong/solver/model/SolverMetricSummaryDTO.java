package me.jsfong.solver.model;
/*
 *
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@JsonRootName("SolverMetricSummaryDTO")
@ToString
public class SolverMetricSummaryDTO implements Serializable {
  
  @Builder.Default
  private String id = UUID.randomUUID().toString();
  
  @Builder.Default
  private String startTime = "";


  @Builder.Default
  private String endTime = "";

  @Builder.Default
  private Long executionTime = 0L;

  @Builder.Default
  private EventStatus status = EventStatus.READY;

  public SolverMetricSummaryDTO() {
  }

  public SolverMetricSummaryDTO(
      @JsonProperty("id") String id,
      @JsonProperty("startTime") String startTime,
      @JsonProperty("endTime") String endTime,
      @JsonProperty("executionTime") Long executionTime,
      @JsonProperty("status") EventStatus status) {
    this.id = id;
    this.startTime = startTime;
    this.endTime = endTime;
    this.executionTime = executionTime;
    this.status = status;
  }
}
