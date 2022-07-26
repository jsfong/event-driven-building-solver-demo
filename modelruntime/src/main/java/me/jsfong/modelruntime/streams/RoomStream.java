package me.jsfong.modelruntime.streams;
/*
 *
 */

import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.model.ElementAggregationDTO;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.model.ElementType;
import me.jsfong.modelruntime.model.JsonDeserializer;
import me.jsfong.modelruntime.model.JsonSerializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.SessionWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomStream {

  @Value("${kafka.element-input-topic}")
  private String FROM_TOPIC;

  @Value("${kafka.aggerated-room-topic}")
  private String TO_TOPIC;

  @Value("${kafka.aggerated-room-session-timeout}")
  private long SESSION_TIMEOUT_SEC;


  @Autowired
  @Qualifier(StreamConfig.ROOM_STEAMS_BUILDER)
  void buildPipeline(StreamsBuilder streamsBuilder) {

    log.info("ElementAggregatorStream - building pipeline");

    //Configure Serde
    JsonSerializer<ElementDTO> dtoJsonSerializer = new JsonSerializer<>();
    JsonDeserializer<ElementDTO> dtoJsonDeserializer = new JsonDeserializer<>(
        ElementDTO.class);
    Serde<ElementDTO> dTOSerde = Serdes.serdeFrom(dtoJsonSerializer,
        dtoJsonDeserializer);

    JsonSerializer<ElementAggregationDTO> dtosJsonSerializer = new JsonSerializer<>();
    JsonDeserializer<ElementAggregationDTO> dtosJsonDeserializer = new JsonDeserializer<>(
        ElementAggregationDTO.class);
    Serde<ElementAggregationDTO> dTOsSerde = Serdes.serdeFrom(dtosJsonSerializer,
        dtosJsonDeserializer);


    try {
      //Get Stream
      var roomStream = streamsBuilder.stream(FROM_TOPIC,
          Consumed.with(Serdes.String(), dTOSerde));

      Duration inactivityGap = Duration.ofSeconds(SESSION_TIMEOUT_SEC);

      //Log Stream
      roomStream
          .filter((k, v) -> v.getType() == ElementType.ROOM)
          .selectKey((k, v) -> getBuildingWatermark(v.getWatermarks()))
          .groupByKey(Grouped.with(Serdes.String(), dTOSerde))
          .windowedBy(SessionWindows.ofInactivityGapWithNoGrace(inactivityGap))
          .aggregate(
              ElementAggregationDTO::new,
              (k, v, agg) -> agg.aggregate(v),
              (k, a, b) -> a.merge(b),
              Materialized.with(Serdes.String(), dTOsSerde)
          ).toStream().to(TO_TOPIC);

// EXAMPLE of reduce
//           .reduce((aggV, newV) -> {
//            log.debug("Reducing: {}", aggV.getElementId());
//
//            String values = aggV.getValues();
//            String newValue = values + "|" + newV.getValues();
//
//            ElementDTO newDto = newV.clone();
//            newDto.setValues(newValue);
//            log.debug("Reducing: {} with value {}", aggV.getElementId(), newValue);
//            return newDto;
//          })
//          .toStream().to(TO_TOPIC);

// Print out debug
//          .foreach(
//          (k, v) -> log.info("RoomStream - Aggregating k: {}, v:{}", k, v.toString()));



    } catch (Exception e) {
      log.error("ElementAggregatorStream - error {}", e.getMessage());
    }

  }

  private String getBuildingWatermark(String watermark) {

    if (StringUtils.isBlank(watermark)) {
      return "";
    }

    String[] split = watermark.split("\\|");
    if (split.length < 3) {
      return "";
    }

    log.debug("getBuildingWatermark: {}", split[2]);
    return split[2];

  }

}
