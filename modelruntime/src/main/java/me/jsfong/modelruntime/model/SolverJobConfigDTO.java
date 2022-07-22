package me.jsfong.modelruntime.model;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import java.util.List;
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
public class SolverJobConfigDTO {

  @Builder.Default
  private String configId = "";

  @Builder.Default
  private ElementType type =  ElementType.INPUT;

  @Builder.Default
  private String causeByElementId = "";

  @Builder.Default
  private String values = "";

  public SolverJobConfig toSolverJobConfig(){
    return SolverJobConfig.builder()
        .configId(this.configId)
        .type(List.of(type.toString()))
        .causeByElementId(this.causeByElementId)
        .values(this.values).build();
  }

}
