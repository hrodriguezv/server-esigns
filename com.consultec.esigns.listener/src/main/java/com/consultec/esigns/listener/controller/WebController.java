package com.consultec.esigns.listener.controller;

import static com.consultec.esigns.listener.util.HttpHeadersUtil.getValueFromHeaderKey;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.listener.config.QueueConfig;
import com.consultec.esigns.listener.queue.MessageSender;
import com.consultec.esigns.listener.util.ListenerErrorTreatmentUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class WebController.
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class WebController {

  /**
   * Convert the object to a json reference and send it to queue to follow the configured flow.
   *
   * @param pobj
   */
  private void receiveFile(PayloadTO pobj) {

    try {

      // serialize and send package to queue in form of a json object
      ObjectMapper objectMapper = new ObjectMapper();
      String pckg = objectMapper.writeValueAsString(pobj);

      MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);

    } catch (Exception e) {

      log.error("Se produjo un error intentando enviar el paquete a la cola", e);

      ListenerErrorTreatmentUtil.treatTheError(pobj, e, true);

    }

  }

  /**
   * Performs required actions to handle the payload received in order to process it as a file on
   * the local file-system.
   *
   * @param pobj
   */
  private void handleFileUpload(PayloadTO pobj) {

    String sessionId;

    try {

      pobj.setStage(Stage.MANUAL_SIGNED);
      sessionId = pobj.getSessionID();

      FileSystemManager manager = FileSystemManager.getInstance();

      manager.init(sessionId);
      manager.createLocalWorkspace(pobj.getStage(), sessionId, pobj.getPlainDocEncoded());

      manager.serializeObjectFile(pobj);

      ObjectMapper objectMapper = new ObjectMapper();
      String pckg = objectMapper.writeValueAsString(pobj);

      MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);

    } catch (Exception e) {

      log.error("Se produjo un error intentando enviar el paquete a la cola", e);

      ListenerErrorTreatmentUtil.treatTheError(pobj, e, true);

    }

  }

  /**
   * Inspects the headers and set the required values to the payload reference.
   *
   * @param payload
   * @param headers
   * 
   * @return
   * 
   */
  private PayloadTO setDefaultValuesToWrapper(PayloadTO payload, HttpHeaders headers) {

    payload.setSessionID(getValueFromHeaderKey(headers, "ngsesid"));
    payload.setSerializedObj(headers);

    return payload;

  }

  @RequestMapping(
      value = "/receive",
      method = {RequestMethod.GET, RequestMethod.POST},
      consumes = {"application/json"})
  public void receive(@RequestBody PayloadTO jsonReference, @RequestHeader HttpHeaders headers) {

    receiveFile(setDefaultValuesToWrapper(jsonReference, headers));

  }

  @RequestMapping(
      value = "/upload",
      method = {RequestMethod.GET, RequestMethod.POST},
      consumes = {"application/json"})
  public void upload(@RequestBody PayloadTO jsonReference, @RequestHeader HttpHeaders headers) {

    handleFileUpload(setDefaultValuesToWrapper(jsonReference, headers));

  }

}
