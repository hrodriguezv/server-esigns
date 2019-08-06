/**
 * 
 */

package com.consultec.esigns.core.util;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(MQUtility.class);

  /**
   * Instantiates a new MQ utility.
   */
  private MQUtility() {

  }

  /**
   * Creates the channel.
   *
   * @param host the host
   * @param serverName the server name
   * @return the channel
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws TimeoutException the timeout exception
   */
  public static Channel createChannel(String host, String serverName)
      throws IOException, TimeoutException {

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
      logger.error("There was an error trying to create a channel ", e);
    }
    return null;
  }

  /**
   * Send message using a Rabbit MQ implementation.
   *
   * @param host the host
   * @param serverName the server name
   * @param message the message
   * @throws Exception the exception
   */
  public static void sendMessage(String host, String serverName, String message) {

    try (Channel channel = createChannel(host, serverName)) {
      if (channel != null)
        channel.basicPublish("", serverName, MessageProperties.PERSISTENT_TEXT_PLAIN,
            message.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      logger.error("There was an error trying to send a message ", e);
    }
  }

  /**
   * Send message using a MQ Active implementation.
   *
   * @param configClazz the config clazz
   * @param senderClazz the sender clazz
   * @param msg the msg
   * @throws JMSException
   */
  @SuppressWarnings("resource")
  public static void sendMessageMQ(Class<?> configClazz, Class<?> senderClazz, String msg)
      throws ConnectException, JMSException {

    AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(configClazz);
    context.register(senderClazz);

    IMessageSender ms = (IMessageSender) context.getBean(senderClazz);
    ms.sendMessage(msg);
  }
}
