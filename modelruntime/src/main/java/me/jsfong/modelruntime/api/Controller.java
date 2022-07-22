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
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class Controller {

  private final StreamsBuilderFactoryBean factoryBean;

  private final ElementInputProducer elementInputProducer;

  private final ElementGraphService elementGraphService;

  @GetMapping("/count/{word}")
  public Long getWordCount(@PathVariable String word) {
    KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();

    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType("counts",
            QueryableStoreTypes.keyValueStore()));

    return counts.get(word);
  }

  /**
   * Create element to element stream
   * @param elementDTO
   */
  @PostMapping("/stream/element")
  public ResponseEntity<ElementDTO> addElementToElementStream(@RequestBody ElementDTO elementDTO)
      throws JsonProcessingException {

    ElementDTO newElement = elementDTO.clone();
    if(StringUtils.isBlank(elementDTO.getElementId())){
      newElement.setElementId(UUID.randomUUID().toString());
    }

    ObjectMapper om = new ObjectMapper();
    elementInputProducer.sendMessage(om.writeValueAsString(newElement));

    return new ResponseEntity(newElement, HttpStatus.CREATED);
  }

  /**
   * Create directly to graph
   * @param elementDTO
   * @return
   */
  @PostMapping("/graph/element")
  public ResponseEntity<ElementDTO> addElementToGraph(@RequestBody ElementDTO elementDTO){
    Element newElement = elementGraphService.createNewElement(elementDTO);
    return new ResponseEntity(newElement, HttpStatus.CREATED);
  }

  @GetMapping("/stream/element/{modelId}")
  public ResponseEntity<List<ElementDTO>> getAllElementsByModelId(@PathVariable String modelId){
    var elements = elementGraphService.getAllElementsByModelId(modelId);
    return new ResponseEntity(elements, HttpStatus.CREATED);
  }

  @DeleteMapping("/stream/element")
  public ResponseEntity<ElementDTO> deleteAll(){
    elementGraphService.deleteAll();
    return new ResponseEntity(null, HttpStatus.OK);
  }
}
