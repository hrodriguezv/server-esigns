package com.consultec.esigns.listener.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.http.HttpHeaders;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.io.IPostHttpClient;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListenerErrorTreatmentUtil {

  private static Map<String, List<String>> transformHeaders(HttpHeaders headers) {

    return headers.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

  }

  @SuppressWarnings("unchecked")
  public static void treatTheError(PayloadTO pobj, Exception e, boolean transformHeader) {

    String msgError = " Hubo un error recibiendo el documento, en la etapa " + pobj.getStage()
        + " del workflow : [" + e.getMessage() + "] sessionid : [" + pobj.getSessionID() + "] - ["
        + e.getMessage() + "]";

    try {

      log.error(msgError, e);
      EventLogger.getInstance().error(msgError);

      IPostHttpClient httpPost = new HttpPostClientImpl(pobj.getCallbackUrl());

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
          + pobj.getSessionID() + "]";

      log.error(msgError, e2);
      EventLogger.getInstance().error(msgError);

    }

  }

}
