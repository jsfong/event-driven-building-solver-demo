package me.jsfong.modelruntime.producer;
/*
 *
 */


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

  @Value("${kafka.element-input-topic}")
  private String ELEMENT_INPUT_TOPIC;

  @Value("${kafka.element-input-topic.partition}")
  private int ELEMENT_INPUT_TOPIC_PARTITION;

  @Value("${kafka.solver-input-topic}")
  private String SOLVER_INPUT_TOPIC;

  @Value("${kafka.solver-input-topic.partition}")
  private int SOLVER_INPUT_TOPIC_PARTITION;

  @Bean
  public NewTopic solverJobInput() {
    return TopicBuilder.name(SOLVER_INPUT_TOPIC)
        .partitions(SOLVER_INPUT_TOPIC_PARTITION)
        .replicas(1)
        .build();

  }


  @Bean
  public NewTopic elementInput() {
    return TopicBuilder.name(ELEMENT_INPUT_TOPIC)
        .partitions(ELEMENT_INPUT_TOPIC_PARTITION)
        .replicas(1)
        .build();

  }
}
