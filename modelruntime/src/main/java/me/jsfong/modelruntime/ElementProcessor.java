package me.jsfong.modelruntime;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.consumer.ConsumerListener;
import me.jsfong.modelruntime.consumer.ElementConsumer;
import me.jsfong.modelruntime.producer.SolverInputProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElementProcessor implements ConsumerListener {

  private SolverInputProducer solverInputProducer;

  private ElementConsumer elementConsumer;

  @Autowired
  public ElementProcessor(SolverInputProducer solverInputProducer, ElementConsumer elementConsumer) {
    this.solverInputProducer = solverInputProducer;
    this.elementConsumer = elementConsumer;
    this.elementConsumer.subscribe(this);
  }

  @Override
  public void update(ConsumerRecord record) {

    String value = (String) record.value();
    log.info("ElementListener - received value: {}", value);

    //Process value
    value += "_P";

    log.info("ElementListener - republish to solver-input");
    solverInputProducer.sendMessage(value);
  }
}
