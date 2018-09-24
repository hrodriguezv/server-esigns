/**
 * 
 */
package com.consultec.esigns.core.util;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.consultec.esigns.core.queue.IMessageSender;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

/**
 * The Class MQUtility.
 *
 * @author hrodriguez
 */
@Configuration
@ComponentScan
public class MQUtility {

	/**
	 * Creates the channel.
	 *
	 * @param host the host
	 * @param serverName the server name
	 * @return the channel
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TimeoutException the timeout exception
	 */
	public static Channel createChannel(String host, String serverName) throws IOException, TimeoutException {
		ConnectionFactory factory = null;
		Connection connection = null;
		try {
			factory = new ConnectionFactory();
			factory.setHost(host);
			connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(serverName, true, false, false, null);
			return channel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Send message using a Rabbit MQ implementation.
	 *
	 * @param host
	 *            the host
	 * @param serverName
	 *            the server name
	 * @param message
	 *            the message
	 * @throws Exception
	 *             the exception
	 */
	public static void sendMessage(String host, String serverName, String message) throws Exception {
		Channel channel = null;
		try {
			channel = createChannel(host, serverName);
			channel.basicPublish("", serverName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + message + "'");
		} catch (Exception e) {
		} finally {
			channel.close();
		}
	}

	/**
	 * Send message using a MQ Active implementation.
	 *
	 * @param configClazz the config clazz
	 * @param senderClazz the sender clazz
	 * @param msg the msg
	 */
	@SuppressWarnings("resource")
	public static void sendMessageMQ(Class<?> configClazz, Class<?> senderClazz, String msg) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configClazz);
		context.register(senderClazz);

		IMessageSender ms = (IMessageSender) context.getBean(senderClazz);
		ms.sendMessage(msg);
	}
}
