package me.jsfong.solver.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ConsumerListener {

  void update(ConsumerRecord record);

}
