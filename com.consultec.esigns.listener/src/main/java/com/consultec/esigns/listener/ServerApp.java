
package com.consultec.esigns.listener;

import static com.consultec.esigns.listener.util.ListenerConstantProperties.KEYSTORE_ACCESS_CONFIGURED;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.security.KeyStoreAccessMode;
import com.consultec.esigns.core.security.SecurityManager;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.lang.GeneralErrorListenerException;
import com.consultec.esigns.listener.storage.StorageProperties;
import com.pheox.jcapi.JCAPIProperties;
import com.pheox.jcapi.JCAPISystemStoreRegistryLocation;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class ServerApp.
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ServerApp {

  /**
   * The main method.
   *
   * @param args the arguments
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TimeoutException the timeout exception
   * @throws NoSuchAlgorithmException
   */
  public static void main(String[] args)
      throws IOException, TimeoutException, NoSuchAlgorithmException {

    SpringApplication.run(ServerApp.class, args);

    char[] pwd = null;

    Optional<String> p12 = Optional.ofNullable(null);

    if (KeyStoreAccessMode.LOCAL_MACHINE.equals(KEYSTORE_ACCESS_CONFIGURED)) {

      JCAPISystemStoreRegistryLocation location = new JCAPISystemStoreRegistryLocation(
          JCAPISystemStoreRegistryLocation.CERT_SYSTEM_STORE_LOCAL_MACHINE);

      JCAPIProperties.getInstance().setSystemStoreRegistryLocation(location);

    } else if (KeyStoreAccessMode.FILE_SYSTEM.equals(KEYSTORE_ACCESS_CONFIGURED)) {

      p12 = Optional.ofNullable(
        PropertiesManager.getInstance().getValue(PropertiesManager.PROPERTY_OPERATOR_CERTIFICATE));

      pwd = PropertiesManager.getInstance().getValue(PropertiesManager.KEY_OPERATOR_CERTIFICATE)
          .toCharArray();

    }

    SecurityManager.getInstance().safeInit(KEYSTORE_ACCESS_CONFIGURED, p12, pwd);

    EventLogger.getInstance().init();

    EventLogger.getInstance()
        .info("Se ha iniciado correctamente el Servicio de registro de firmas electronicas");

    log.info("ServerApp listener started ...");

  }

  /**
   * Broker.
   *
   * @return the broker service
   * @throws GeneralErrorListenerException
   * @throws Exception the exception
   */
  @Bean
  public BrokerService broker() throws GeneralErrorListenerException {

    try {

      PropertiesManager props = PropertiesManager.getInstance();
      BrokerService broker = new BrokerService();

      broker.addConnector(props.getValue(PropertiesManager.QUEUE_SERVER_HOST) + ":"
          + props.getValue(PropertiesManager.QUEUE_SERVER_PORT));

      log.info("Starting broker Service at port : {}", PropertiesManager.QUEUE_SERVER_PORT);

      return broker;

    } catch (Exception e) {

      throw new GeneralErrorListenerException("Error starting broker service", e);

    }

  }

}
