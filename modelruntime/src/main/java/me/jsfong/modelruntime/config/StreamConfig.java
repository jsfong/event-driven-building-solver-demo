package me.jsfong.modelruntime.config;
/*
 *
 */

import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@Configuration
@EnableKafka
public class StreamConfig {

  private static final String APP_ID = "element-aggregator";

  private static final String BOOT_STRAP_SERVERS = "localhost:9092";

  public static final String WORD_COUNT_STEAMS_BUILDER = "wordCountStreamBuilder";

  @Bean(name = WORD_COUNT_STEAMS_BUILDER)
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




}
