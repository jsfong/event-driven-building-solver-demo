package me.jsfong.solver.stream;
/*
 *
 */

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@EnableKafka
public class StreamConfig {

  @Value(value = "${kafka.solver.metrics.app-id}")
  private String APP_ID;

  @Value(value = "${kafka.bootstrap-servers}")
  private String BOOT_STRAP_SERVERS;

  public static final String STEAMS_BUILDER = "metrics-aggregator-builder";

  @Bean(name = STEAMS_BUILDER)
  @Profile("learn-stream")
  public StreamsBuilderFactoryBean metricsStreamBuilder() {
    var streamConfig = new KafkaStreamsConfiguration(Map.of(
        StreamsConfig.APPLICATION_ID_CONFIG, APP_ID,
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVERS,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
//        StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0"
//        StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE
//        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Bytes().getClass(),
//        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()
    ));

    return new StreamsBuilderFactoryBean(streamConfig);
  }


}
