package me.jsfong.solver.model;
/*
 * 
 */

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("JobConfig")
@Getter
@Setter
@Builder
public class SolverJobConfig {
  @Id
  @GeneratedValue
  private Long id;

  private String configId;

  private String modelId;

  private String watermark;

  private List<String> type;

  private List<String> causeByElementId;

  private String values;

}
