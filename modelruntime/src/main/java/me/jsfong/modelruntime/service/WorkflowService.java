package me.jsfong.modelruntime.service;
/*
 * 
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.consumer.ElementListener;
import me.jsfong.modelruntime.consumer.ElementConsumer;
import me.jsfong.modelruntime.consumer.RoomsConsumer;
import me.jsfong.modelruntime.model.ElementAggregationDTO;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.model.ElementType;
import me.jsfong.modelruntime.model.SolverJobConfigDTO;
import me.jsfong.modelruntime.producer.SolverJobConfigProducer;
import net.minidev.json.JSONArray;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowService implements ElementListener {

  private ElementConsumer elementConsumer;

  private RoomsConsumer roomsConsumer;

  private SolverJobConfigProducer solverJobConfigProducer;

  public WorkflowService(ElementConsumer elementConsumer,
      RoomsConsumer roomsConsumer, SolverJobConfigProducer solverJobConfigProducer) {
    this.elementConsumer = elementConsumer;
    this.roomsConsumer = roomsConsumer;
    this.solverJobConfigProducer = solverJobConfigProducer;
    this.elementConsumer.subscribe(this);
    this.roomsConsumer.subscribe(this);
  }

  @Override
  public void update(String consumerName, ConsumerRecord record) {

    solverTriggerWorkflow(consumerName, record);
  }

  private void solverTriggerWorkflow(String consumerName, ConsumerRecord record) {

    String value = (String) record.value();
    log.info("solverTriggerWorkflow - received value: {}", value);

    //Deserialize to DTO
    try {
      ObjectMapper om = new ObjectMapper();
      var solverJobConfigDTOS = new ArrayList<SolverJobConfigDTO>();

      //Generate config

      //Element
      if(consumerName.equals(ElementConsumer.class.getName())){
        var elementDTO = om.readValue(value, ElementDTO.class);
        solverJobConfigDTOS.addAll(generateSolverJobConfig(elementDTO));

        for(SolverJobConfigDTO dto: solverJobConfigDTOS){

          //Publish to stream
          log.info("solverTriggerWorkflow - generated {} config", dto.getType().toString());
          log.info("solverTriggerWorkflow - publishing config to stream");
          String msg = om.writeValueAsString(dto);
          solverJobConfigProducer.sendMessage(msg);
        }
      }

      //Aggregation of element
      if(consumerName.equals(RoomsConsumer.class.getName())){
        var elementDTO = om.readValue(value, ElementAggregationDTO.class);

        var elements = new ArrayList<>(elementDTO.getElementDTOS());
        var elementIds = elementDTO.getElementDTOS().stream().map(ElementDTO::getElementId)
            .collect(Collectors.toList());

        //ROOM
        SolverJobConfigDTO dto = SolverJobConfigDTO.builder()
            .configId(UUID.randomUUID().toString())
            .type(ElementType.AREA)
            .causeByElementId(elementIds)
            .modelId(elementDTO.getModelId())
            .watermark(" ROOMS")
            .values(JSONArray.toJSONString(elements))
            .build();

        //Publish to stream
        log.info("solverTriggerWorkflow - generated {} config", dto.getType().toString());
        log.info("solverTriggerWorkflow - publishing config to stream");
        String msg = om.writeValueAsString(dto);
        solverJobConfigProducer.sendMessage(msg);
      }

    } catch (JsonProcessingException e) {
      log.info("solverTriggerWorkflow - unable to parse");
    }

  }


  private List<SolverJobConfigDTO> generateSolverJobConfig(ElementDTO dto) {

    ArrayList<SolverJobConfigDTO> solverJobConfigs = new ArrayList<>();

    //INPUT -> SITE -> BUILDING -> LEVEL -> ROOM -> AREA
    switch (dto.getType()) {
      case INPUT:
        solverJobConfigs.add(createConfig(ElementType.SITE, dto));
        break;
      case SITE:
        solverJobConfigs.add(createConfig(ElementType.BUILDING, dto));
        break;
      case BUILDING:
        solverJobConfigs.add(createConfig(ElementType.LEVEL, dto));
        break;
      case LEVEL:
        solverJobConfigs.add(createConfig(ElementType.ROOM, dto));
        break;

      default:
        break;
    }

    return solverJobConfigs;
  }
  private SolverJobConfigDTO createConfig(ElementType type, ElementDTO dto){

    return SolverJobConfigDTO.builder()
        .configId(UUID.randomUUID().toString())
        .type(type)
        .causeByElementId(List.of(dto.getElementId()))
        .modelId(dto.getModelId())
        .watermark(dto.getWatermarks())
        .values(dto.getType().toString() + " 1")
        .build();
  }

  private SolverJobConfigDTO createConfig(ElementType type,ElementDTO dto, String msg){

    return SolverJobConfigDTO.builder()
        .configId(UUID.randomUUID().toString())
        .type(type)
        .causeByElementId(List.of(dto.getElementId()))
        .modelId(dto.getModelId())
        .watermark(dto.getWatermarks())
        .values(dto.getType().toString() + " " + msg)
        .build();
  }

}
