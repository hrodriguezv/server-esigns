package com.consultec.esigns.listener;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.util.PropertiesManager;

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
	 */
	public static void main(String[] args) throws IOException, TimeoutException {
		@SuppressWarnings("unused")
		ApplicationContext context = SpringApplication.run(ServerApp.class, args);
		EventLogger.getInstance().init();
		EventLogger.getInstance().info("Se ha iniciado correctamente el Servicio de registro de firmas electronicas");
		logger.info("ServerApp listener started ...");
	}

	/**
	 * Broker.
	 *
	 * @return the broker service
	 * @throws Exception
	 *             the exception
	 */
	@Bean
	public BrokerService broker() throws Exception {
		PropertiesManager props = PropertiesManager.getInstance();
		BrokerService broker = new BrokerService();
		broker.addConnector(props.getValue(PropertiesManager.QUEUE_SERVER_HOST) + ":"
				+ props.getValue(PropertiesManager.QUEUE_SERVER_PORT));
		logger.info("Starting broker Service at port :" + PropertiesManager.QUEUE_SERVER_PORT);
		return broker;
	}
}
