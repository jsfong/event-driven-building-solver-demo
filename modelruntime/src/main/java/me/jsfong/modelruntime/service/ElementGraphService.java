package me.jsfong.modelruntime.service;
/*
 * 
 */

import java.util.List;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;

public interface ElementGraphService {

  List<Element> getAllServices();

  List<ElementDTO> getAllElementsByModelId(String modelId);

  Element getElementByElementId(String elementId);

  ElementDTO createNewElement (ElementDTO element);

  ElementDTO updateElement(ElementDTO elementDTO);
  List<ElementDTO> getAllElementsFromElement(String elementId);

  List<ElementDTO> getAllElementsWithModelIdAndType(String modelId, String type);

  List<ElementDTO> getElements(String modelId, String commonParentType, String type);

  ElementDTO getLastNodeWIthModelIdAndType(String modelId, String type);

  void deleteAll();

  void deleteAllFromElement(String elementId);
}
