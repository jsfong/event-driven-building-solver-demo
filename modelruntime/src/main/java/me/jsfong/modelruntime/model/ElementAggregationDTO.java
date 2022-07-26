package me.jsfong.modelruntime.model;
/*
 *
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElementAggregationDTO {

  @Builder.Default
  private String elementId = "";

  @Builder.Default
  private String modelId = "";

  @Builder.Default
  private Set<String> parentElementId = new HashSet<>();

  @Builder.Default
  private Set<String> childElementId = new HashSet<>();

  @Builder.Default
  private ElementType type = ElementType.INPUT;

  @Builder.Default
  private String watermarks = "";

  @Builder.Default
  private Set<ElementDTO> elementDTOS = new HashSet<>();

  public ElementAggregationDTO clone() {
    var clonedElementDTOs = elementDTOS.stream().map(ElementDTO::clone)
        .collect(Collectors.toSet());

    return ElementAggregationDTO.builder()
        .elementId(this.elementId)
        .modelId(this.modelId)
        .parentElementId(new HashSet<>(this.parentElementId))
        .childElementId(new HashSet<>(this.childElementId))
        .type(this.type)
        .elementDTOS(clonedElementDTOs)
        .watermarks(this.watermarks)
        .build();
  }

  public ElementAggregationDTO merge(ElementAggregationDTO dto) {

    log.debug("ElementAggregationDTO - merge {}", dto.getWatermarks());
    this.elementId = UUID.randomUUID().toString();
    this.parentElementId.addAll(dto.parentElementId);
    this.childElementId.addAll(dto.childElementId);
    this.watermarks = this.watermarks + " | " + dto.getWatermarks();
    this.elementDTOS.addAll(dto.getElementDTOS());
    return this;

  }

  public ElementAggregationDTO aggregate (ElementDTO dto){

    log.debug("ElementAggregationDTO - aggregate {}", dto.getWatermarks());
    this.elementId = UUID.randomUUID().toString();
    this.parentElementId.addAll(dto.getParentElementId());
    this.childElementId.addAll(dto.getChildElementId());
    this.watermarks = "";
    this.elementDTOS.add(dto);
    return this;
  }

}
