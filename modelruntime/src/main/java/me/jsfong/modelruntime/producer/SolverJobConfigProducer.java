package me.jsfong.modelruntime.producer;
/*
 * 
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SolverJobConfigProducer {

  private KafkaTemplate<String, String> kafkaTemplate;
  @Value("${kafka.solver-input-topic}")
  private String SOLVER_INPUT_TOPIC;

  @Autowired
  public SolverJobConfigProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String msg){
    kafkaTemplate.send(SOLVER_INPUT_TOPIC, msg);
  }
}
