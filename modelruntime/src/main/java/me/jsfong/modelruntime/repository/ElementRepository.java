package me.jsfong.modelruntime.repository;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import java.util.List;
import me.jsfong.modelruntime.model.Element;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

@RepositoryRestResource(collectionResourceRel = "Element", path = "neo4j")
public interface ElementRepository extends Neo4jRepository<Element, Long> {

  @Query(value = "MATCH (a:Element),(b:Element)\n" +
      "WHERE a.elementId = :#{#parent_element_id} AND b.elementId = :#{#child_element_id}\n" +
      "CREATE (a)-[r:HAS_CHILD]->(b)")
  @Transactional
  void createHasChildRelationship(
      @Param("parent_element_id") String parent_element_id,
      @Param("child_element_id") String child_id);

  @Query(value = "MATCH (a:Element)\n" +
      "WHERE a.modelId = :#{#modelId}\n" +
      "RETURN a")
  List<Element> findElementByModelId(String modelId);

  @Query(value = "MATCH (a:Element)\n" +
      "WHERE a.elementId = :#{#element_id}\n" +
      "RETURN a\n" +
      "LIMIT 1")
  Element findElementByElementId(String element_id);


  @Query(value = "MATCH (a:Element)\n" +
      "WHERE a.elementId = :#{#element_id} \n" +
      "SET a.#{#property} = #{#value}")
  void updateStringProperty(
      @Param("element_id") String element_id,
      @Param("property") String property,
      @Param("value") String value);

}
