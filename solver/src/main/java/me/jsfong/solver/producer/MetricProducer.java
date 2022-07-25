package me.jsfong.solver.producer;
/*
 * 
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MetricProducer {

  private KafkaTemplate<String, String> kafkaTemplate;

  @Value(value = "${kafka.solver-metrics-event-topic}")
  private String METRICS_TOPIC;

  @Autowired
  public MetricProducer(@Qualifier("metricProducerTemplate") KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(String msg){
    kafkaTemplate.send(METRICS_TOPIC, msg);
  }
}
