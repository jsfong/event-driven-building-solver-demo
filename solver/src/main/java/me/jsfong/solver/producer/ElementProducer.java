package me.jsfong.solver.producer;
/*
 *
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ElementProducer {

  private KafkaTemplate<String, String> kafkaTemplate;

  @Value(value = "${kafka.element-input-topic}")
  private String ELEMENT_INPUT_TOPIC;

  @Autowired
  public ElementProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String msg){
    kafkaTemplate.send(ELEMENT_INPUT_TOPIC, msg);
  }

  public void sendMessageWithKey(String key, String msg){
    kafkaTemplate.send(ELEMENT_INPUT_TOPIC, key, msg);
  }
}
