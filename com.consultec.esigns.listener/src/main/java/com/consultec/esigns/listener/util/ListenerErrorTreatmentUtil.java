package com.consultec.esigns.listener.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.io.IPostHttpClient;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListenerErrorTreatmentUtil {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(ListenerErrorTreatmentUtil.class);

  private static Map<String, List<String>> transformHeaders(HttpHeaders headers) {
    Map<String, List<String>> map = headers.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return map;
  }

  @SuppressWarnings("unchecked")
  public static void treatTheError(PayloadTO pobj, Exception e, boolean transformHeader) {

    String msgError = " Hubo un error recibiendo el documento, en la etapa " + pobj.getStage()
        + " del workflow : [" + e.getMessage() + "] sessionid : [" + pobj.getSessionID() + "] - ["
        + e.getMessage() + "]";

    try {

      logger.error(msgError, e);
      EventLogger.getInstance().error(msgError);
      IPostHttpClient httpPost = new HttpPostClientImpl(
          pobj.getOrigin() + "/o/api/account-opening/receive-status");
      pobj.setStage(Stage.ERROR);
      ObjectMapper objectMapper = new ObjectMapper();
      String pckg = objectMapper.writeValueAsString(pobj);
      HttpEntity stringEntity = new StringEntity(pckg, ContentType.APPLICATION_JSON);

      httpPost.setEntity(stringEntity);
      httpPost.setCookie(pobj.getCookieHeader());
      if (transformHeader) {

        httpPost.fillHeader(transformHeaders((HttpHeaders) pobj.getSerializedObj()));

      } else {

        httpPost.fillHeader((LinkedHashMap<String, List<String>>) pobj.getSerializedObj());

      }

      httpPost.execute();

    } catch (Exception e2) {

      msgError = "Error intentando enviar el error de vuelta a Stella sessionid : ["
          + pobj.getSessionID() + "] - [" + e2.getMessage() + "]";
      logger.error(msgError, e2);
      EventLogger.getInstance().error(msgError);

    }

  }

}
