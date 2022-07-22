package me.jsfong.solver.stream;
/*
 *
 */


import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.solver.model.JsonDeserializer;
import me.jsfong.solver.model.JsonSerializer;
import me.jsfong.solver.model.SolverMetricDTO;
import me.jsfong.solver.model.SolverMetricSummaryDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class metricsAggregationStream {

  @Value("${kafka.solver-metrics-event-topic}")
  private String FROM_TOPIC;

  @Value("${kafka.solver-metrics-agg-topic}")
  private String TO_TOPIC;

  @Autowired
  @Qualifier(StreamConfig.STEAMS_BUILDER)
  void buildPipeline(StreamsBuilder streamsBuilder) {

    log.info("ElementAggregatorStream - building pipeline");

    //Configure Serde
    JsonSerializer<SolverMetricDTO> metricJsonSerializer = new JsonSerializer<>();
    JsonDeserializer<SolverMetricDTO> metricJsonDeserializer = new JsonDeserializer<>(
        SolverMetricDTO.class);
    Serde<SolverMetricDTO> solverMetricDTOSerde = Serdes.serdeFrom(metricJsonSerializer,
        metricJsonDeserializer);

    JsonSerializer<SolverMetricSummaryDTO> metricSummaryJsonSerializer = new JsonSerializer();
    JsonDeserializer<SolverMetricSummaryDTO> metricSummaryJsonDeserializer = new JsonDeserializer<>(
        SolverMetricSummaryDTO.class);
    Serde<SolverMetricSummaryDTO> jsonSerde = Serdes.serdeFrom(metricSummaryJsonSerializer,
        metricSummaryJsonDeserializer);

    try {
      //Get Stream
      var metricStream = streamsBuilder.stream(FROM_TOPIC,
          Consumed.with(Serdes.String(), solverMetricDTOSerde));

      //Log Stream
      metricStream.foreach(
          (k, v) -> log.info("ElementAggregatorStream - Aggregating k: {}, v:{}", k, v.toString()));

      var initialMetric = SolverMetricSummaryDTO.builder().build();

      var metricAgg = metricStream
          .selectKey((k, v) -> v.getModelId())
          .groupByKey(Grouped.with(Serdes.String(), solverMetricDTOSerde))
          .aggregate(
              () -> initialMetric,
              metricsAggregationStream::newAgg,
              Materialized.<String, SolverMetricSummaryDTO, KeyValueStore<Bytes, byte[]>>as(
                      "metric")
                  .withKeySerde(Serdes.String())
                  .withValueSerde(jsonSerde)
          );

      metricAgg.toStream().to(TO_TOPIC, Produced.with(Serdes.String(), jsonSerde));

    } catch (Exception e) {
      log.error("ElementAggregatorStream - error {}", e.getMessage());
    }

  }


  private static SolverMetricSummaryDTO newAgg(String key, SolverMetricDTO dto,
      SolverMetricSummaryDTO summary) {

    //Status
    summary.setStatus(dto.getStatus());

    //Timestamp
    if(StringUtils.isBlank(summary.getStartTime())){
      summary.setStartTime(dto.getTimeStamp());
      summary.setEndTime(dto.getTimeStamp());
      return summary;
    }

    var endTimeInstant = Instant.parse(summary.getEndTime());
    var currentInstant = Instant.parse(dto.getTimeStamp());

    if(currentInstant.compareTo(endTimeInstant) > 0){
      summary.setEndTime(dto.getTimeStamp());
    }

    var summaryStart =  Instant.parse(summary.getStartTime()).toEpochMilli();
    var summaryEnd =  Instant.parse(summary.getEndTime()).toEpochMilli();
    summary.setExecutionTime(summaryEnd - summaryStart);

    log.info("newAgg - summaryStart [{}] [{}] : {}",  key, dto.getSolverType().toString(), summary.getStartTime());
    log.info("newAgg - summaryEnd [{}] [{}]: {}", key, dto.getSolverType().toString(),  summary.getEndTime());
    log.info("newAgg - Exec time [{}] [{}]: {}", key, dto.getSolverType().toString(),  summary.getExecutionTime());

    return summary;
  }
}
