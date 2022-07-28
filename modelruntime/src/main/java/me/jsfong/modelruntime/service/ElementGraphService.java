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

  Element createNewElement (ElementDTO element);

  List<ElementDTO> getAllElementsFromElement(String elementId);

  void deleteAll();
}
