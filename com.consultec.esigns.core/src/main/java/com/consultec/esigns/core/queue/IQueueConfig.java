/**
 * 
 */
package com.consultec.esigns.core.queue;

import javax.jms.ConnectionFactory;

import org.springframework.jms.config.JmsListenerContainerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Interface IQueueConfig.
 *
 * @author hrodriguez
 */
public interface IQueueConfig {

	/**
	 * Connection factory.
	 *
	 * @return the connection factory
	 */
	ConnectionFactory connectionFactory();

	/**
	 * Jms listener container factory.
	 *
	 * @return the jms listener container factory
	 */
	@SuppressWarnings("rawtypes")
	JmsListenerContainerFactory jmsListenerContainerFactory(); 
}
