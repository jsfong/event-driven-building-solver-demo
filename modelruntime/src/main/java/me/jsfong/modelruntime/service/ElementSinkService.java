package me.jsfong.modelruntime.service;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.consumer.ElementListener;
import me.jsfong.modelruntime.consumer.ElementConsumer;
import me.jsfong.modelruntime.model.ElementDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElementSinkService implements ElementListener {

  private ElementGraphService elementGraphService;

  private ElementConsumer elementConsumer;

  @Autowired
  public ElementSinkService(ElementGraphService elementGraphService,
      ElementConsumer elementConsumer) {
    this.elementGraphService = elementGraphService;
    this.elementConsumer = elementConsumer;
    this.elementConsumer.subscribe(this);
  }

  @Override
  public void update(ConsumerRecord record) {
    String value = (String) record.value();
    log.info("ElementSink - received value: {}", value);

    //Deserialize to DTO
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      ElementDTO elementDTO = objectMapper.readValue(value, ElementDTO.class);

      log.info("ElementSink - Sink to element graph db");
      elementGraphService.createNewElement(elementDTO);

    } catch (JsonProcessingException e) {
      log.error("ElementSink - unable to parse element");
    }


  }
}
