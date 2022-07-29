package me.jsfong.modelruntime.api;
/*
 *
 */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.producer.ElementInputProducer;
import me.jsfong.modelruntime.service.ElementGraphService;
import me.jsfong.modelruntime.service.Workflow;
import me.jsfong.modelruntime.service.WorkflowService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class Controller {

  private final ElementInputProducer elementInputProducer;

  private final ElementGraphService elementGraphService;

  private final WorkflowService workflowService;

  //  private final StreamsBuilderFactoryBean factoryBean;

//  @GetMapping("/count/{word}")
//  public Long getWordCount(@PathVariable String word) {
//    KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
//
//    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
//        StoreQueryParameters.fromNameAndType("counts",
//            QueryableStoreTypes.keyValueStore()));
//
//    return counts.get(word);
//  }

//  /**
//   * Create element to element stream
//   *
//   * @param elementDTO
//   */
//  @PostMapping("/stream/element")
//  public ResponseEntity<ElementDTO> addElementToElementStream(@RequestBody ElementDTO elementDTO)
//      throws JsonProcessingException {
//
//
//    ElementDTO newElement = new ElementDTO();
//    newElement.setModelId(elementDTO.getModelId());
//    newElement.setType(elementDTO.getType());
//    newElement.setValues(elementDTO.getValues());
//    newElement.setCauseBy(elementDTO.getCauseBy());
//
//    if (StringUtils.isBlank(elementDTO.getElementId())) {
//      newElement.setElementId(UUID.randomUUID().toString());
//    }
//
//    ObjectMapper om = new ObjectMapper();
//    elementInputProducer.sendMessageWithKey(newElement.getModelId(),
//        om.writeValueAsString(newElement));
//
//    return new ResponseEntity(newElement, HttpStatus.CREATED);
//  }

  /**
   * Create directly to graph
   *
   * @param elementDTO
   * @return
   */
  @PostMapping("/graph/element")
  public ResponseEntity<ElementDTO> addElementToGraph(@RequestBody ElementDTO elementDTO)
      throws JsonProcessingException {
    var newElement = workflowService.createElement(elementDTO);
    return new ResponseEntity(newElement, HttpStatus.CREATED);
  }

  @PatchMapping("/graph/element")
  public ResponseEntity<ElementDTO> updateElementOnGraph(@RequestBody ElementDTO elementDTO)
      throws JsonProcessingException {
    workflowService.updateElement(elementDTO);
    return new ResponseEntity(null, HttpStatus.OK);
  }
  @GetMapping("/stream/element/{modelId}")
  public ResponseEntity<List<ElementDTO>> getAllElementsByModelId(@PathVariable String modelId) {
    var elements = elementGraphService.getAllElementsByModelId(modelId);
    return new ResponseEntity(elements, HttpStatus.CREATED);
  }
  @DeleteMapping("/graph/element/all")
  public ResponseEntity<ElementDTO> deleteAll() {
    elementGraphService.deleteAll();
    return new ResponseEntity(null, HttpStatus.OK);
  }

  @DeleteMapping("/graph/element/{elementId}")
  public ResponseEntity<ElementDTO> deleteElement(@PathVariable String elementId) {
    elementGraphService.deleteAllFromElement(elementId);
    return new ResponseEntity(null, HttpStatus.OK);
  }

  @GetMapping("/stream/path/{elementId}")
  public ResponseEntity<List<ElementDTO>> getAllElementsFromElementWithId(@PathVariable String elementId) {
    var elements = elementGraphService.getAllElementsFromElement(elementId);
    return new ResponseEntity(elements, HttpStatus.OK);
  }

  @GetMapping("/stream/path/{modelId}/{type}")
  public ResponseEntity<List<ElementDTO>> getLastNodeWIthModelIdAndType(
      @PathVariable String modelId,
      @PathVariable String type) {
    var elements = elementGraphService.getLastNodeWIthModelIdAndType(modelId, type);
    return new ResponseEntity(elements, HttpStatus.OK);
  }
}
