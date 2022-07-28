package me.jsfong.modelruntime.service;
/*
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;
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
  public Element createNewElement(ElementDTO elementDTO) {
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

    return currElement;
  }

  @Override
  public List<ElementDTO> getAllElementsFromElement(String elementId) {
    log.info("ElementGraphService - getAllElementsFromElement");

    List<Element> elements = elementRepository.getPathToEndFromElementId(elementId);
    return elements.stream().map(Element::toElementDTO).collect(Collectors.toList());
  }

  @Override
  public void deleteAll() {
    log.info("ElementGraphService - deleteAll");
    elementRepository.deleteAll();
  }
}
