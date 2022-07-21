package me.jsfong.modelruntime.api;
/*
 *
 */


import lombok.AllArgsConstructor;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.producer.ElementInputProducer;
import me.jsfong.modelruntime.service.ElementGraphService;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
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

  @PostMapping("/stream/element")
  public void addElementToElementStream(@RequestBody String msg){
    elementInputProducer.sendMessage(msg);
  }

  @PostMapping("/graph/element")
  public ResponseEntity<ElementDTO> addElementToGraph(@RequestBody ElementDTO elementDTO){
    Element newElement = elementGraphService.createNewElement(elementDTO);
    return new ResponseEntity(newElement, HttpStatus.CREATED);
  }

}
