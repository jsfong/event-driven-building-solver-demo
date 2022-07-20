package me.jsfong.modelruntime.producer;
/*
 * 
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SolverInputProducer {

  private KafkaTemplate<String, String> kafkaTemplate;

  private static final String SOLVER_INPUT_TOPIC = "solver-input";

  @Autowired
  public SolverInputProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String msg){
    kafkaTemplate.send(SOLVER_INPUT_TOPIC, msg);
  }
}
