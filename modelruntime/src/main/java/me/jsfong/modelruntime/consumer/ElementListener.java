package me.jsfong.modelruntime.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface ElementListener {

  void update(ConsumerRecord record);

}
