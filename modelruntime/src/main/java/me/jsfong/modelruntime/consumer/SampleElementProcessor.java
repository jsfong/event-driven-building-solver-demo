package me.jsfong.modelruntime.consumer;
/*
 *
 */

import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.producer.SolverJobConfigProducer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SampleElementProcessor implements ElementListener {

  private SolverJobConfigProducer solverJobConfigProducer;

  private ElementConsumer elementConsumer;

  @Autowired
  public SampleElementProcessor(SolverJobConfigProducer solverJobConfigProducer, ElementConsumer elementConsumer) {
    this.solverJobConfigProducer = solverJobConfigProducer;
    this.elementConsumer = elementConsumer;
//    this.elementStreamPublisher.subscribe(this);
  }

  @Override
  public void update(ConsumerRecord record) {

    String value = (String) record.value();
    log.info("ElementProcessor - received value: {}", value);

    //Process value
    value += "_P";

    log.info("ElementProcessor - republish to solver-input");
    solverJobConfigProducer.sendMessage(value);
  }
}
