package me.jsfong.modelruntime.producer;
/*
 *
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ElementInputProducer {

  private KafkaTemplate<String, String> kafkaTemplate;

  @Value(value = "${kafka.element-input-topic}")
  private String ELEMENT_INPUT_TOPIC;

  @Autowired
  public ElementInputProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String msg){
    kafkaTemplate.send(ELEMENT_INPUT_TOPIC, msg);
  }
}
