/**
 * 
 */
package com.consultec.esigns.core.events;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.consultec.esigns.core.queue.HandlerQueue;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.core.util.PropertiesManager;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;



// TODO: Auto-generated Javadoc
/**
 * The Class EventController.
 */
public class EventController implements Consumer {

	/** The handlers. */
	private List<HandlerQueue> handlers;
	
	/** The instance. */
	private static volatile EventController instance;
	/** The mutex. */
	private static Object mutex = new Object();

	
	/**
	 * Gets the single instance of EventController.
	 *
	 * @return single instance of EventController
	 */
	public static EventController getInstance() {
		EventController result = instance;
		if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new EventController();
			}
		}
		return result;
	}

	/**
	 * Instantiates a new event controller.
	 */
	private EventController() {
	}


	/**
	 * Inits the.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TimeoutException the timeout exception
	 */
	public void init() throws IOException, TimeoutException {
		instance.handlers = new ArrayList<>();
		String queueName = PropertiesManager.getInstance().getValue(PropertiesManager.QUEUE_SERVER_NAME);
		Channel channel = MQUtility.createChannel("localhost", queueName);
		channel.basicConsume(queueName, true, this);
	}

	/* (non-Javadoc)
	 * @see com.rabbitmq.client.Consumer#handleConsumeOk(java.lang.String)
	 */
	@Override
	public void handleConsumeOk(String consumerTag) {
	}

	/* (non-Javadoc)
	 * @see com.rabbitmq.client.Consumer#handleCancelOk(java.lang.String)
	 */
	@Override
	public void handleCancelOk(String consumerTag) {
	}

	/* (non-Javadoc)
	 * @see com.rabbitmq.client.Consumer#handleCancel(java.lang.String)
	 */
	@Override
	public void handleCancel(String consumerTag) throws IOException {
	}

	/* (non-Javadoc)
	 * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
	 */
	@Override
	public void handleDelivery(String arg0, Envelope arg1, BasicProperties arg2, byte[] arg3) throws IOException {
		fireHandlerQueueEvent(arg3);
	}

	/**
	 * Fire handler queue event.
	 *
	 * @param arg3 the arg 3
	 */
	private void fireHandlerQueueEvent(byte[] arg3) {
		for (HandlerQueue b : instance.handlers) {
			b.handleQueue(arg3);
		}

	}

	/* (non-Javadoc)
	 * @see com.rabbitmq.client.Consumer#handleShutdownSignal(java.lang.String, com.rabbitmq.client.ShutdownSignalException)
	 */
	@Override
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
	}

	/* (non-Javadoc)
	 * @see com.rabbitmq.client.Consumer#handleRecoverOk(java.lang.String)
	 */
	@Override
	public void handleRecoverOk(String consumerTag) {
	}
	
	/**
	 * Subscribe.
	 *
	 * @param e the e
	 */
	public void subscribe(HandlerQueue e) {
		instance.handlers.add(e);
	}

}
