package me.jsfong.modelruntime.api;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */


import lombok.AllArgsConstructor;
import me.jsfong.modelruntime.producer.ElementInputProducer;
import me.jsfong.modelruntime.producer.SolverInputProducer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
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


  @GetMapping("/count/{word}")
  public Long getWordCount(@PathVariable String word) {
    KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();

    ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
        StoreQueryParameters.fromNameAndType("counts",
            QueryableStoreTypes.keyValueStore()));

    return counts.get(word);
  }

  @PostMapping("/element")
  public void addElement(@RequestBody String msg){
    elementInputProducer.sendMessage(msg);
  }

}
