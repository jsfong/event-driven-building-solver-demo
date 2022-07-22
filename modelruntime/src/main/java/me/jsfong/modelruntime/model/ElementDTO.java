package me.jsfong.modelruntime.model;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
public class ElementDTO {

  @Builder.Default
  private String elementId = "";

  @Builder.Default
  private String modelId = "";

  @Builder.Default
  private List<String> parentElementId = new ArrayList<>();
  @Builder.Default
  private List<String> childElementId = new ArrayList<>();

  @Builder.Default
  private ElementType type = ElementType.INPUT;
  @Builder.Default
  private String watermarks = "";

  @Builder.Default
  private String values = "";

  public ElementDTO clone() {
    return ElementDTO.builder()
        .elementId(this.elementId)
        .modelId(this.modelId)
        .parentElementId(new ArrayList<>(this.parentElementId))
        .childElementId(new ArrayList<>(this.childElementId))
        .type(this.type)
        .values(this.values)
        .watermarks(this.watermarks)
        .build();
  }

  public Element toElement() {
    return Element.builder()
        .type(List.of(this.type.toString()))
        .elementId(this.elementId)
        .modelId(this.modelId)
        .watermarks(this.watermarks)
        .values(this.values)
        .parentElementId(new ArrayList<>(this.parentElementId))
        .childElementId(new ArrayList<>(this.childElementId))
        .build();
  }

}
