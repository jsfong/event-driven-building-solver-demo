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
@JsonRootName("SolverMetricDTO")
@ToString
public class SolverMetricDTO implements Serializable {

  @Builder.Default
  private String id = UUID.randomUUID().toString();

  @Builder.Default
  private String modelId = "";

  @Builder.Default
  private ElementType solverType = ElementType.NONE;

  @Builder.Default
  private EventStatus status = EventStatus.READY;

  @Builder.Default
  private String timeStamp =Instant.now().toString();

  public SolverMetricDTO() {
  }

  public SolverMetricDTO(
      @JsonProperty("id") String id,
      @JsonProperty("modelId")  String modelId,
      @JsonProperty("solverType")  ElementType solverType,
      @JsonProperty("status")  EventStatus status,
      @JsonProperty("timeStamp") String timeStamp) {
    this.id = id;
    this.modelId = modelId;
    this.solverType = solverType;
    this.status = status;
    this.timeStamp = timeStamp;
  }
}
