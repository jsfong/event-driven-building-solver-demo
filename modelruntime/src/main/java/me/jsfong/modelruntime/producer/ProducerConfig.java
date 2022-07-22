package me.jsfong.modelruntime.producer;
/*
 *
 */

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
public class ProducerConfig {

  @Value(value = "${kafka.bootstrap-servers}")
  private String BOOT_STRAP_SERVERS;

  @Bean
  public ProducerFactory<String, String> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    configProps.put(BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVERS);
    configProps.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    configProps.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    // producer acks
    configProps.put(ACKS_CONFIG,"all");
//    configProps.put(RETRIES_CONFIG, 3);
    configProps.put(LINGER_MS_CONFIG, "1");
    configProps.put(ENABLE_IDEMPOTENCE_CONFIG, "true");  // ensure we don't push duplicates

    return new DefaultKafkaProducerFactory<>(configProps);
  }

  @Bean
  public KafkaTemplate<String, String> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
