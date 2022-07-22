package me.jsfong.modelruntime.consumer;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.service.ElementGraphService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElementSink  implements ElementListener {

  private ElementGraphService elementGraphService;

  private ElementStreamPublisher elementStreamPublisher;

  @Autowired
  public ElementSink(ElementGraphService elementGraphService,
      ElementStreamPublisher elementStreamPublisher) {
    this.elementGraphService = elementGraphService;
    this.elementStreamPublisher = elementStreamPublisher;
    this.elementStreamPublisher.subscribe(this);
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
