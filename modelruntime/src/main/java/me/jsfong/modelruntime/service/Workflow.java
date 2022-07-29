package me.jsfong.modelruntime.service;
/*
 * Copyright(c) Lendlease Corporation, all rights reserved
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import me.jsfong.modelruntime.model.Element;
import me.jsfong.modelruntime.model.ElementDTO;

public interface Workflow {

  ElementDTO createElement(ElementDTO elementDTO) throws JsonProcessingException;

  ElementDTO updateElement(ElementDTO elementDTO) throws JsonProcessingException;

}
