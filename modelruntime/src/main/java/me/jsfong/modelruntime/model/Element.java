package me.jsfong.modelruntime.model;
/*
 *
 */

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;


@Node
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Element {

  @Id
  @GeneratedValue
  private Long id;

  private String elementId;

  private String modelId;

  @Relationship(type = "HAS_CHILD", direction = Direction.OUTGOING)
  private Set<Element> childElement = new HashSet<>();
}
