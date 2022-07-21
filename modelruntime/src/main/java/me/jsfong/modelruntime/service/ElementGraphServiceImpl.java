package me.jsfong.modelruntime.service;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;
import me.jsfong.modelruntime.repository.ElementRepository;
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
  public Element createNewElement(ElementDTO elementDTO) {
    log.info("ElementGraphService - creatNewElement");
    Element element = new Element();
    element.setModelId(elementDTO.getModelId());

    Element newElement = elementRepository.save(element);

    if(elementDTO.getParentId() != null){
      elementRepository.createHasChildRelationship(elementDTO.getParentId(), newElement.getId());
    }
    return null;
  }
}
