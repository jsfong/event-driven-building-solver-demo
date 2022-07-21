package me.jsfong.modelruntime.service;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import java.util.List;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;

public interface ElementGraphService {

  List<Element> getAllServices();

  Element createNewElement (ElementDTO element);
}
