package me.jsfong.modelruntime.service;
/*
 *
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.consumer.ElementListener;
import me.jsfong.modelruntime.consumer.ElementConsumer;
import me.jsfong.modelruntime.consumer.RoomsConsumer;
import me.jsfong.modelruntime.model.CauseBy;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementAggregationDTO;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.model.ElementType;
import me.jsfong.modelruntime.model.SolverJobConfigDTO;
import me.jsfong.modelruntime.producer.ElementInputProducer;
import me.jsfong.modelruntime.producer.SolverJobConfigProducer;
import net.minidev.json.JSONArray;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkflowService implements ElementListener, Workflow {

  private ElementConsumer elementConsumer;
  private RoomsConsumer roomsConsumer;
  private SolverJobConfigProducer solverJobConfigProducer;

  private ElementInputProducer elementInputProducer;
  private ElementGraphService elementGraphService;
  private ObjectMapper om = new ObjectMapper();

  public WorkflowService(ElementConsumer elementConsumer,
      RoomsConsumer roomsConsumer, SolverJobConfigProducer solverJobConfigProducer,
      ElementInputProducer elementInputProducer, ElementGraphService elementGraphService) {
    this.elementConsumer = elementConsumer;
    this.roomsConsumer = roomsConsumer;
    this.solverJobConfigProducer = solverJobConfigProducer;
    this.elementInputProducer = elementInputProducer;
    this.elementGraphService = elementGraphService;
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

    try {
      //Normal solver
      if (consumerName.equals(ElementConsumer.class.getName())) {
        processNormalWorkflow(value);
      }

      //Aggregation solver
      if (consumerName.equals(RoomsConsumer.class.getName())) {
        processAggregationWorkflow(value);
      }
    } catch (JsonProcessingException e) {
      log.info("solverTriggerWorkflow - unable to parse");
    }

  }

  private void processNormalWorkflow(String value) throws JsonProcessingException {

    var solverJobConfigDTOS = new ArrayList<SolverJobConfigDTO>();
    var elementDTO = om.readValue(value, ElementDTO.class);
    solverJobConfigDTOS.addAll(generateSolverJobConfig(elementDTO));

    for (SolverJobConfigDTO dto : solverJobConfigDTOS) {
      //Publish to stream
      log.info("solverTriggerWorkflow - generated {} config", dto.getType().toString());
      log.info("solverTriggerWorkflow - publishing config to stream");
      String msg = om.writeValueAsString(dto);
      solverJobConfigProducer.sendMessage(msg);
    }
  }


  private void processAggregationWorkflow(String value) throws JsonProcessingException {

    //ROOM aggregation
    var elementDTO = om.readValue(value, ElementAggregationDTO.class);

    //Check if aggregation is already happened
    //1. Identify modelId
    var modelId = elementDTO.getModelId();
    //2. Identify the common parent
    var commonParent = ElementType.BUILDING;
    //4. Check last node element is aggregated element type
    var targetedAggregatedType = ElementType.AREA;
    var areaNode = elementGraphService.getElements(
        modelId, commonParent.toString(), targetedAggregatedType.toString());


    //5. Delete aggregation result if not delete
    if (areaNode != null && areaNode.size() >1) {
      log.info(
          "solverTriggerWorkflow - processAggregationWorkflow: detected aggregation already done.");

      //Delete aggregated result
      elementGraphService.deleteAllFromElement(areaNode.get(0).getElementId());
      log.info("solverTriggerWorkflow - processAggregationWorkflow: deleted aggregation result.");
    }

    //6. Checking contain all aggregate element -> aggregate session element + existing element
    var allRoomElements = elementGraphService.getElements(modelId, commonParent.toString(),
        ElementType.ROOM.toString());

    //7. Perform remove duplicate
    HashMap<String, ElementDTO> uniqueRooms = new HashMap<>();
    allRoomElements.forEach(r -> uniqueRooms.put(r.getElementId(), r));
    elementDTO.getElementDTOS().forEach(r -> uniqueRooms.put(r.getElementId(), r));
    log.info(
        "solverTriggerWorkflow - processAggregationWorkflow: Complete aggregation with {} of room, trigger aggregation solver.",
        uniqueRooms.size());

    var elements = new ArrayList<>(uniqueRooms.values());
    var elementIds = uniqueRooms.values().stream().map(ElementDTO::getElementId)
        .collect(Collectors.toList());

    SolverJobConfigDTO dto = SolverJobConfigDTO.builder()
        .configId(UUID.randomUUID().toString())
        .type(ElementType.AREA)
        .causeByElementId(elementIds)
        .modelId(elementDTO.getModelId())
        .watermark(" ROOMS")
        .causeBy(elementDTO.getCauseBy())
        .values(JSONArray.toJSONString(elements))
        .build();

    //Publish to stream
    log.info("solverTriggerWorkflow - generated {} config", dto.getType().toString());
    log.info("solverTriggerWorkflow - publishing config to stream");
    String msg = om.writeValueAsString(dto);
    solverJobConfigProducer.sendMessage(msg);
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

  private SolverJobConfigDTO createConfig(ElementType type, ElementDTO dto) {

    return SolverJobConfigDTO.builder()
        .configId(UUID.randomUUID().toString())
        .type(type)
        .causeByElementId(List.of(dto.getElementId()))
        .modelId(dto.getModelId())
        .watermark(dto.getWatermarks())
        .values(dto.getValues())
        .causeBy(dto.getCauseBy())
        .build();
  }

  private SolverJobConfigDTO createConfig(ElementType type, ElementDTO dto, String msg) {

    return SolverJobConfigDTO.builder()
        .configId(UUID.randomUUID().toString())
        .type(type)
        .causeByElementId(List.of(dto.getElementId()))
        .modelId(dto.getModelId())
        .watermark(dto.getWatermarks())
        .values(dto.getType().toString() + " " + msg)
        .causeBy(dto.getCauseBy())
        .build();
  }

  @Override
  public ElementDTO createElement(ElementDTO elementDTO) throws JsonProcessingException {
    log.info("WorkflowService - createElement");

    var newElement = elementDTO.clone();
    newElement.setElementId(UUID.randomUUID().toString());
    newElement.setCauseBy(CauseBy.INPUT);

    //Publish to element_input
    log.info("WorkflowService - createElement - publish to stream");
    ObjectMapper om = new ObjectMapper();
    elementInputProducer.sendMessageWithKey(newElement.getModelId(),
        om.writeValueAsString(newElement));

    return newElement;
  }

  @Override
  public ElementDTO updateElement(ElementDTO elementDTO) throws JsonProcessingException {

    //Delete old element and create new element
    var updateElement = elementGraphService.updateElement(elementDTO);

    //Publish to element_input
    log.info("WorkflowService - updateElement - publish to stream");
    ObjectMapper om = new ObjectMapper();
    elementInputProducer.sendMessageWithKey(updateElement.getModelId(),
        om.writeValueAsString(updateElement));

    return updateElement;
  }
}
