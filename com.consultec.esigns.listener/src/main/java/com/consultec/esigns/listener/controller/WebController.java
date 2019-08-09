package com.consultec.esigns.listener.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The default Rest Controller.
 */
@Controller
public class WebController {

  /**
   * Index.
   *
   * @return the string
   */
  @GetMapping("/index")
  public String index() {
    return "index";
  }

}
