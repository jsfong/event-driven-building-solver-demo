package me.jsfong.solver.model;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SolverMetricDTO {

  @Builder.Default
  private String id = UUID.randomUUID().toString();

  @Builder.Default
  private String modelId = "";

  @Builder.Default
  private ElementType solverType = ElementType.NONE;

  @Builder.Default
  private EventStatus status = EventStatus.READY;


}
