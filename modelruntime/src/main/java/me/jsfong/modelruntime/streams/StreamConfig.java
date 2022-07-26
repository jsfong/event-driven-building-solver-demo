package me.jsfong.modelruntime.streams;
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

@Configuration
@EnableKafka
public class StreamConfig {

  private static final String APP_ID = "element-aggregator";

  @Value(value = "${kafka.bootstrap-servers}")
  private String BOOT_STRAP_SERVERS;

  public static final String WORD_COUNT_STEAMS_BUILDER = "wordCountStreamBuilder";
  public static final String ROOM_STEAMS_BUILDER = "roomStreamBuilder";

  @Bean(name = WORD_COUNT_STEAMS_BUILDER)
  @Profile("learn-stream")
  public StreamsBuilderFactoryBean wordCountStreamBuilder() {
    var streamConfig = new KafkaStreamsConfiguration(Map.of(
        StreamsConfig.APPLICATION_ID_CONFIG, APP_ID,
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVERS,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass(),
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()
    ));

    return new StreamsBuilderFactoryBean(streamConfig);
  }

  @Bean(name = ROOM_STEAMS_BUILDER)
  public StreamsBuilderFactoryBean roomStreamBuilder() {
    var streamConfig = new KafkaStreamsConfiguration(Map.of(
        StreamsConfig.APPLICATION_ID_CONFIG, APP_ID,
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVERS,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
//        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass(),
//        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass()
    ));

    return new StreamsBuilderFactoryBean(streamConfig);
  }


}
