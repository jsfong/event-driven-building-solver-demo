package me.jsfong.modelruntime.consumer;
/*
 *
 */

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.model.ElementAggregationDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoomsConsumer {

  private List<ElementListener> listeners = new ArrayList<>();

  @KafkaListener(topics = "${kafka.aggerated-room-topic}", groupId = "model-runtime-room-consumer")
  public void consumerRoomsRecord(ConsumerRecord<String, ElementAggregationDTO> record) {
    log.info("consumerRoomsRecord - Key: {}, Value: {}", record.key(), record.value());
    log.info("consumerRoomsRecord - Partition: {}, Offset: {}", record.partition(), record.offset());
    notifyListener(record);
  }

  public void subscribe(ElementListener listener) {
    this.listeners.add(listener);
  }


  private void notifyListener(ConsumerRecord<String, ElementAggregationDTO> record){
    listeners.forEach(l -> l.update(this.getClass().getName(), record));
  }
}
