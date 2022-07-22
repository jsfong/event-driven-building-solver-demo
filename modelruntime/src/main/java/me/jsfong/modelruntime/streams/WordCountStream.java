package me.jsfong.modelruntime.streams;
/*
 *
 */


import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WordCountStream {

  @Value("${kafka.element-input-topic}")
  private String ELEMENT_INPUT_TOPIC;

  @Autowired
  @Qualifier(StreamConfig.WORD_COUNT_STEAMS_BUILDER)
  void buildPipeline(StreamsBuilder streamsBuilder) {

    log.info("ElementAggregatorStream - building pipeline");

    KStream<String, String> elementStream = streamsBuilder.stream(ELEMENT_INPUT_TOPIC,
        Consumed.with(Serdes.String(), Serdes.String()));

    KTable<String, Long> wordCounts = elementStream.mapValues(v -> v.toLowerCase())
        .flatMapValues(v -> Arrays.asList(v.split("\\W+")))
        .selectKey((k, v) -> v)
        .groupByKey()
        .count(Materialized.as("counts"));

    wordCounts.toStream().to("word-count", Produced.with(Serdes.String(), Serdes.Long()));


  }
}
