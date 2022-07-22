package me.jsfong.modelruntime.service;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.consumer.ElementListener;
import me.jsfong.modelruntime.consumer.ElementConsumer;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.model.ElementType;
import me.jsfong.modelruntime.model.SolverJobConfigDTO;
import me.jsfong.modelruntime.producer.SolverJobConfigProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowService implements ElementListener {

  private ElementConsumer elementConsumer;

  private SolverJobConfigProducer solverJobConfigProducer;

  public WorkflowService(ElementConsumer elementConsumer,
      SolverJobConfigProducer solverJobConfigProducer) {
    this.elementConsumer = elementConsumer;
    this.solverJobConfigProducer = solverJobConfigProducer;
    this.elementConsumer.subscribe(this);
  }

  @Override
  public void update(ConsumerRecord record) {
    solverTriggerWorkflow(record);
  }

  private void solverTriggerWorkflow(ConsumerRecord record) {

    String value = (String) record.value();
    log.info("solverTriggerWorkflow - received value: {}", value);

    //Deserialize to DTO
    ElementDTO elementDTO = null;
    try {
      ObjectMapper om = new ObjectMapper();
      elementDTO = om.readValue(value, ElementDTO.class);

      //Generate config
      var solverJobConfigDTOS = generateSolverJobConfig(elementDTO);
      solverJobConfigDTOS.forEach(dto -> {
        log.info("solverTriggerWorkflow - generated {} config", dto.getType().toString());
      });

      //Publish to stream
      log.info("solverTriggerWorkflow - publishing config to stream");
      String msg = om.writeValueAsString(solverJobConfigDTOS);
      solverJobConfigProducer.sendMessage(msg);


    } catch (JsonProcessingException e) {
      log.info("solverTriggerWorkflow - unable to parse");
    }

  }


  private List<SolverJobConfigDTO> generateSolverJobConfig(ElementDTO dto) {

    ArrayList<SolverJobConfigDTO> solverJobConfigs = new ArrayList<>();

    //INPUT -> SITE -> BUILDING -> LEVEL -> ROOM -> AREA
    switch (dto.getType()) {
      case INPUT:
        solverJobConfigs.add(createConfig(ElementType.SITE, dto.getElementId()));
        break;
      case SITE:
        solverJobConfigs.add(createConfig(ElementType.BUILDING, dto.getElementId()));
      case BUILDING:
        solverJobConfigs.add(createConfig(ElementType.ROOM, dto.getElementId(), "1"));
        solverJobConfigs.add(createConfig(ElementType.ROOM, dto.getElementId(), "2"));
      case LEVEL:
        solverJobConfigs.add(createConfig(ElementType.ROOM, dto.getElementId(), "1"));
        solverJobConfigs.add(createConfig(ElementType.ROOM, dto.getElementId(), "2"));
        break;

      default:
        break;
    }

    return solverJobConfigs;
  }

  private SolverJobConfigDTO createConfig(ElementType type, String elementId) {
    return new SolverJobConfigDTO(
        UUID.randomUUID().toString(),
        type,
        elementId,
        type.toString() + " 1");
  }

  private SolverJobConfigDTO createConfig(ElementType type, String elementId, String msg) {
    return new SolverJobConfigDTO(
        UUID.randomUUID().toString(),
        type,
        elementId,
        type.toString() + msg);
  }
}
