package me.jsfong.solver.model;
/*
 *
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.DynamicLabels;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;


@Node
@Getter
@Setter
@Builder
public class Element {

  @Id
  @GeneratedValue
  private Long id;

  @DynamicLabels
  private List<String> type;

  private String elementId;

  private String modelId;

  private String watermarks;

  private String values;

  @Builder.Default
  private List<String> parentElementId = new ArrayList<>();

  @Builder.Default
  private List<String> childElementId = new ArrayList<>();

  @Relationship(type = "HAS_CHILD", direction = Direction.OUTGOING)
  private Set<Element> childElement = new HashSet<>();

  @Relationship(type = "HAS_CHILD", direction = Direction.INCOMING)
  private Set<Element> parentElement = new HashSet<>();

  public ElementDTO toElementDTO() {
    return ElementDTO.builder()
        .elementId(this.elementId)
        .modelId(this.modelId)
        .parentElementId(
            parentElement.stream().map(Element::getElementId).collect(Collectors.toList()))
        .childElementId(
            childElement.stream().map(Element::getElementId).collect(Collectors.toList()))
        .type(ElementType.valueOf(this.type.get(0).toUpperCase()))
        .watermarks(this.watermarks)
        .values(this.values)
        .parentElementId(new ArrayList<>(this.parentElementId))
        .childElementId(new ArrayList<>(this.childElementId))
        .build();

  }
}
