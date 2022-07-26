package me.jsfong.solver.consumer;
/*
 *
 */

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SolverJobConsumer {

  private List<ConsumerListener> listeners = new ArrayList<>();

  //Create multiple KafkaListner is equal to create multiple consumer
//  @KafkaListener(topics = "element-input", groupId = "element-consumer")
//  public void listenToElementValue(String message){
//    log.info("Value: {}", message);
//  }

  @KafkaListener(topics = "${kafka.solver-input-topic}", groupId = "solver-job-consumer")
  public void consumerElementRecord(ConsumerRecord<String, String> record) {
    log.info("Key: {}, Value: {}", record.key(), record.value());
    log.info("Partition: {}, Offset: {}", record.partition(), record.offset());
    notifyListener(record);
  }

  public void subscribe(ConsumerListener listener) {
    this.listeners.add(listener);
  }


  private void notifyListener(ConsumerRecord<String, String> record){
    listeners.forEach(l -> l.update(record));
  }
}


