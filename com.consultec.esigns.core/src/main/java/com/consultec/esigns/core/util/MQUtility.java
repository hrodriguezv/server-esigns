package com.consultec.esigns.core.util;

import java.net.ConnectException;

import javax.jms.JMSException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.consultec.esigns.core.queue.IMessageSender;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class MQUtility.
 *
 * @author hrodriguez
 */
@Slf4j
@Configuration
@ComponentScan
public class MQUtility {

  /**
   * Send message using a MQ Active implementation.
   *
   * @param configClazz the config clazz
   * @param senderClazz the sender clazz
   * @param msg the msg
   * 
   * @throws JMSException
   */
  @SuppressWarnings("resource")
  public static void sendMessageMQ(Class<?> configClazz, Class<?> senderClazz, String msg)
      throws ConnectException, JMSException {

    AnnotationConfigApplicationContext context =
        new AnnotationConfigApplicationContext(configClazz);

    context.register(senderClazz);

    IMessageSender ms = (IMessageSender) context.getBean(senderClazz);

    log.debug("Sending message: {}", msg);

    ms.sendMessage(msg);

  }

}
