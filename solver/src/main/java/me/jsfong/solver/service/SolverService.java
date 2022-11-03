package me.jsfong.solver.service;
/*
 *
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.solver.consumer.ConsumerListener;
import me.jsfong.solver.consumer.SolverJobConsumer;
import me.jsfong.solver.model.ElementDTO;
import me.jsfong.solver.model.EventStatus;
import me.jsfong.solver.model.SolverJobConfigDTO;
import me.jsfong.solver.model.SolverMetricDTO;
import me.jsfong.solver.model.SolverMetricDTO.SolverMetricDTOBuilder;
import me.jsfong.solver.producer.ElementProducer;
import me.jsfong.solver.producer.MetricProducer;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SolverService implements ConsumerListener {

  private final SolverJobConsumer solverJobConsumer;

  private final ElementProducer elementProducer;

  private final MetricProducer metricProducer;

  @Value(value = "${solver.stimulate-solving-time}")
  private  int STIMULATE_PROCESSING_TIME_MS;

  @Autowired
  public SolverService(SolverJobConsumer solverJobConsumer, ElementProducer elementProducer,
      MetricProducer metricProducer) {
    this.solverJobConsumer = solverJobConsumer;
    this.elementProducer = elementProducer;
    this.metricProducer = metricProducer;
    this.solverJobConsumer.subscribe(this);
  }

  @Override
  public void update(ConsumerRecord record) {
    stimulateSolverSolving(record);
  }


  private void stimulateSolverSolving(ConsumerRecord record) {
    String value = (String) record.value();
    log.info("SolverService - received value: {}", value);

    //Deserialize to DTO
    try {
      ObjectMapper om = new ObjectMapper();
      var solverJobConfigDTO = om.readValue(value, SolverJobConfigDTO.class);

      //Send metric
      var metric = SolverMetricDTO.builder()
          .modelId(solverJobConfigDTO.getModelId())
          .solverType(solverJobConfigDTO.getType())
          .status(EventStatus.SOLVING)
          .timeStamp(Instant.now().toString())
          .build();
      metricProducer.sendMessage(om.writeValueAsString(metric));

      log.info("SolverService - [{}] Stimulated Start solving at {} ",
          solverJobConfigDTO.getType().toString(), Instant.now().toString());


      //Solving
      long t = System.currentTimeMillis();
      long end = t + STIMULATE_PROCESSING_TIME_MS;
      while (System.currentTimeMillis() < end) {
        log.info("SolverService - [{}] Stimulated solving...remaining {}",
            solverJobConfigDTO.getType().toString(), end - System.currentTimeMillis());
        Thread.sleep(STIMULATE_PROCESSING_TIME_MS / 10);
      }
      log.info("SolverService - [{}] Stimulated DONE solving at {} ",
          solverJobConfigDTO.getType().toString(), Instant.now().toString());

      //Send metric
      metric = SolverMetricDTO.builder()
          .modelId(solverJobConfigDTO.getModelId())
          .solverType(solverJobConfigDTO.getType())
          .status(EventStatus.DONE)
          .timeStamp(Instant.now().toString())
          .build();
      metricProducer.sendMessage(om.writeValueAsString(metric));

      //Generate output
      List<ElementDTO> elementDTOS = processAndOutputSolverResult(solverJobConfigDTO);
      elementDTOS.forEach(dto -> log.info("SolverService - output result for {}", dto.getType()));

      //Publish to stream
      for (ElementDTO dto : elementDTOS) {
        elementProducer.sendMessageWithKey(dto.getModelId(), om.writeValueAsString(dto));
      }

    } catch (JsonProcessingException e) {
      log.info("SolverService - unable to parse");
    } catch (InterruptedException e) {

    }
  }


  private List<ElementDTO> processAndOutputSolverResult(SolverJobConfigDTO dto)
      throws JsonProcessingException {
    log.info("SolverService - processAndOutputSolverResult");

    ArrayList<ElementDTO> elementDTOS = new ArrayList<>();

    //INPUT -> SITE -> BUILDING -> LEVEL -> ROOM -> AREA
    switch (dto.getType()) {
      case LEVEL:
        elementDTOS.add(createElementDTO(dto, "level 1"));
        elementDTOS.add(createElementDTO(dto, "level 2"));
        break;
      case ROOM:
        elementDTOS.add(createElementDTO(dto, "room 1 from " + dto.getValues()));
        elementDTOS.add(createElementDTO(dto, "room 2 from " + dto.getValues()));
        break;

      case AREA:

        var jString = dto.getValues();
        var om = new ObjectMapper();
        var elementArray = om.readValue(jString, ElementDTO[].class);
        var elements = Arrays.asList(elementArray);
        var totalValue = elements.stream().map(ElementDTO::getValues)
            .reduce("", (total, curr) -> total + " | " + curr);
        elementDTOS.add(createElementDTO(dto, totalValue));
        break;
      default:
        elementDTOS.add(createElementDTO(dto));
        break;
    }

    return elementDTOS;
  }

  private ElementDTO createElementDTO(SolverJobConfigDTO jobConfig) {

    String watermark = jobConfig.getWatermark();
    if (StringUtils.isBlank(watermark)) {
      watermark = jobConfig.getModelId();
    }

    var elementId = UUID.randomUUID().toString();

    return ElementDTO.builder()
        .elementId(elementId)
        .modelId(jobConfig.getModelId())
        .parentElementId(jobConfig.getCauseByElementId())
        .type(jobConfig.getType())
        .watermarks(watermark + "|" + jobConfig.getType().toString() + ":"+ elementId)
        .causeBy(jobConfig.getCauseBy())
        .values(jobConfig.getType().toString() + " value")
        .build();
  }

  private ElementDTO createElementDTO(SolverJobConfigDTO jobConfig, String msg) {

    String watermark = jobConfig.getWatermark();
    if (StringUtils.isBlank(watermark)) {
      watermark = jobConfig.getModelId();
    }

    var elementId = UUID.randomUUID().toString();

    return ElementDTO.builder()
        .elementId(elementId)
        .modelId(jobConfig.getModelId())
        .parentElementId(jobConfig.getCauseByElementId())
        .type(jobConfig.getType())
        .watermarks(watermark + "|" + jobConfig.getType().toString() +  ":"+ elementId)
        .causeBy(jobConfig.getCauseBy())
        .values(msg)
        .build();
  }
}









