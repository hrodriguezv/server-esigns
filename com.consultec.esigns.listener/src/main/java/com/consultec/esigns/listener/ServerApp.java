
package com.consultec.esigns.listener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.lang.GeneralErrorListenerException;

/**
 * The Class ServerApp.
 */
@SpringBootApplication
public class ServerApp {

	private static final Logger logger =
		LoggerFactory.getLogger(ServerApp.class);

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TimeoutException
	 *             the timeout exception
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args)
		throws IOException, TimeoutException, NoSuchAlgorithmException {

		SpringApplication.run(ServerApp.class, new String[] {});
		EventLogger.getInstance().init();
		EventLogger.getInstance().info(
			"Se ha iniciado correctamente el Servicio de registro de firmas electronicas");
		logger.info("ServerApp listener started ...");
	}

	/**
	 * Broker.
	 *
	 * @return the broker service
	 * @throws GeneralErrorListenerException
	 * @throws Exception
	 *             the exception
	 */
	@Bean
	public BrokerService broker()
		throws GeneralErrorListenerException {

		try {
			PropertiesManager props = PropertiesManager.getInstance();
			BrokerService broker = new BrokerService();
			broker.addConnector(
				props.getValue(PropertiesManager.QUEUE_SERVER_HOST) + ":" +
					props.getValue(PropertiesManager.QUEUE_SERVER_PORT));
			logger.info(
				"Starting broker Service at port :" +
					PropertiesManager.QUEUE_SERVER_PORT);
			return broker;
		}
		catch (Exception e) {
			throw new GeneralErrorListenerException(
				"Error starting broker service", e);
		}
	}
}
