package com.consultec.esigns.listener.config;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;

import com.consultec.esigns.core.queue.IQueueConfig;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.queue.MessageSender;

/**
 * The Class QueueConfig.
 */
@Configuration
@ComponentScan
@EnableJms
public class QueueConfig implements IQueueConfig {

	/** The Constant QUEUE_NAME. */
	public static final String QUEUE_NAME = PropertiesManager.getInstance()
			.getValue(PropertiesManager.QUEUE_SERVER_NAME);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.consultec.esigns.core.queue.IQueueConfig#connectionFactory()
	 */
	@Bean
	public ConnectionFactory connectionFactory() {
		PropertiesManager pref = PropertiesManager.getInstance();
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(pref.getValue(PropertiesManager.QUEUE_SERVER_HOST) + ":"
				+ pref.getValue(PropertiesManager.QUEUE_SERVER_PORT));
		return connectionFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.consultec.esigns.core.queue.IQueueConfig#jmsListenerContainerFactory()
	 */
	@SuppressWarnings("rawtypes")
	@Bean
	public JmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory());
		// core poll size=4 threads and max poll size 8 threads
		factory.setConcurrency("4-8");
		return factory;
	}

	/**
	 * Send message MQ.
	 *
	 * @param msg
	 *            the msg
	 */
	public void sendMessageMQ(String msg) {
		@SuppressWarnings("resource")
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(QueueConfig.class);
		context.register(MessageSender.class);
		MessageSender ms = context.getBean(MessageSender.class);
		ms.sendMessage(msg);
	}
}