package me.jsfong.solver.model;
/*
 * 
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
  private String modelId = "";


  @Builder.Default
  private String watermark = "";

  @Builder.Default
  private String values = "";

  public SolverJobConfig toSolverJobConfig(){
    return SolverJobConfig.builder()
        .configId(this.configId)
        .modelId(this.modelId)
        .watermark(this.watermark)
        .type(List.of(type.toString()))
        .causeByElementId(this.causeByElementId)
        .values(this.values).build();
  }
}
