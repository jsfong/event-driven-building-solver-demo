package me.jsfong.modelruntime.service;
/*
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.model.CauseBy;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.model.ElementType;
import me.jsfong.modelruntime.repository.ElementRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class ElementGraphServiceImpl implements ElementGraphService {

  private final ElementRepository elementRepository;

  public ElementGraphServiceImpl(ElementRepository elementRepository) {
    log.info("Starting ElementGraphService");
    this.elementRepository = elementRepository;
  }


  @Override
  public List<Element> getAllServices() {
    List<Element> elements = new ArrayList<>();
    Iterable<Element> allElements = elementRepository.findAll();
    allElements.forEach(elements::add);
    return elements;
  }

  @Override
  public List<ElementDTO> getAllElementsByModelId(String modelId) {
    log.info("ElementGraphService - getAllElementsByModelId");

    if (StringUtils.isBlank(modelId)) {
      return Collections.emptyList();
    }

    List<Element> elementByModelId = elementRepository.findElementByModelId(modelId);
    return elementByModelId.stream().map(Element::toElementDTO).collect(Collectors.toList());

  }

  @Override
  public Element getElementByElementId(String elementId) {
    log.info("ElementGraphService - getElementByElementId");
    return elementRepository.findElementByElementId(elementId);
  }

  @Override
  public ElementDTO createNewElement(ElementDTO elementDTO) {
    log.info("ElementGraphService - creatNewElement");
    Element element = elementDTO.toElement();
    Element currElement = elementRepository.save(element);

    log.info("ElementGraphService - successfully save");

    List<String> parentElementId = elementDTO.getParentElementId();
    if (parentElementId != null && !parentElementId.isEmpty()) {
      log.info("ElementGraphService - constructing parent id");
      parentElementId.forEach(
          parentId -> {
            //Find parent and create relationship
            elementRepository.createHasChildRelationship(parentId, currElement.getElementId());

            //TODO Update parent's child
          });


    }

    List<String> childElementId = elementDTO.getChildElementId();
    if (childElementId != null && !childElementId.isEmpty()) {
      log.info("ElementGraphService - constructing child id");
      childElementId.forEach(
          childId -> {
            //Create relationship to child
            elementRepository.createHasChildRelationship(currElement.getElementId(),
                childId);

            //TODO Update parent's child
          });
    }

    return elementDTO;
  }

  @Override
  public ElementDTO updateElement(ElementDTO elementDTO) {
    log.info("ElementGraphService - updateElement");

    //1. Check if element exist
    var oldElement = elementRepository.findElementByElementId(
        elementDTO.getElementId());

    if(oldElement == null){
      log.info("ElementGraphService - updateElement. Element not found, create new");
      return  createNewElement(elementDTO);
    }

    log.info("ElementGraphService - updateElement. Found element");

    //2. Prepare the updated element
    log.info("ElementGraphService - updateElement. Prepare the updated element");
    var elementId = UUID.randomUUID().toString();
    var newElement = elementDTO;
    newElement.setElementId(elementId);
    newElement.setModelId(oldElement.getModelId());
    newElement.setParentElementId(oldElement.getParentElementId());
    newElement.setChildElementId(Collections.emptyList());
    newElement.setType(ElementType.valueOf(oldElement.getType()));

    var newWatermark = Arrays.asList(oldElement.getWatermarks().split("\\|"));
    var newWatermarkSize = newWatermark.size();
    StringBuilder sb = new StringBuilder();
    for(int i=0; i<newWatermarkSize - 1; i++){
      sb.append(newWatermark.get(i));
      sb.append("|");
    }
    sb.append(newElement.getType() + ":" + elementId);
    newElement.setWatermarks(sb.toString());
    newElement.setCauseBy(CauseBy.UPDATE);

    //3. Delete old element and all the element below
    log.info("ElementGraphService - updateElement. Deleting element {}", oldElement.getElementId());
    elementRepository.deleteAllFromElement(oldElement.getElementId());

    return newElement;

  }


  @Override
  public List<ElementDTO> getAllElementsFromElement(String elementId) {
    log.info("ElementGraphService - getAllElementsFromElement");

    List<Element> elements = elementRepository.getPathToEndFromElementId(elementId);
    return elements.stream().map(Element::toElementDTO).collect(Collectors.toList());
  }

  @Override
  public List<ElementDTO> getAllElementsWithModelIdAndType(String modelId, String type) {
    log.info("ElementGraphService - getAllElementsWithModelIdAndType");
    List<Element> elements = elementRepository.getPathToEndFromModelIdAndType(modelId, type);
    return elements.stream().map(Element::toElementDTO).collect(Collectors.toList());
  }

  @Override
  public List<ElementDTO> getElements(String modelId, String commonParentType, String type) {
    log.info("ElementGraphService - getAllElementsWithModelIdAndType");
    List<Element> elements = elementRepository.getElementByModelId_ParentType_type(modelId, commonParentType, type);
    return elements.stream().map(Element::toElementDTO).collect(Collectors.toList());
  }

  @Override
  public ElementDTO getLastNodeWIthModelIdAndType(String modelId, String type) {
    log.info("ElementGraphService - getLastNodeWIthModelIdAndType");
    var element = elementRepository.getLastNodeWithModelIdAndType(modelId, type);
    return element.toElementDTO();
  }

  @Override
  public void deleteAll() {
    log.info("ElementGraphService - deleteAll");
    elementRepository.deleteAll();
  }

  @Override
  public void deleteAllFromElement(String elementId) {
    log.info("ElementGraphService - deleteAllFromElement");
    elementRepository.deleteAllFromElement(elementId);
  }
}
