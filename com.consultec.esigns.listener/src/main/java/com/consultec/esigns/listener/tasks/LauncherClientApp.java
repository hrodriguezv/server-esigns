/**
 * 
 */

package com.consultec.esigns.listener.tasks;

import static com.consultec.esigns.listener.util.ListenerConstantProperties.launchClientApp;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.consultec.esigns.core.events.EventLogger;

import lombok.extern.slf4j.Slf4j;

/**
 * @author hrodriguez
 */
@Slf4j
public class LauncherClientApp implements Callable<Integer> {

  private String sessionId;

  /**
   * 
   */
  public LauncherClientApp(String session) {
    this.sessionId = session;
  }

  @Override
  public Integer call() throws Exception {

    int value = -99;

    try {

      value = launchClientApp(sessionId);

      EventLogger.getInstance()
          .info("Se inicia el proceso de firma digital. Session ID: [" + sessionId + "]");

    } catch (IOException e) {

      log.error("There was an error trying start the launcher of PDF viewer:[{}]", e.getMessage(),
        e);

      EventLogger.getInstance()
          .error("Hubo un error ejecutando el launcher del visualizador de PDF's : ["
              + e.getMessage() + "]");

    } catch (InterruptedException e) {

      log.error("There was an error trying start the launcher of PDF viewer:[{}]", e.getMessage(),
        e);

      EventLogger.getInstance()
          .error("Hubo un error ejecutando el launcher del visualizador de PDF's : ["
              + e.getMessage() + "]");

      Thread.currentThread().interrupt();

    }

    return value;

  }

}
