/**
 * 
 */
package com.consultec.esigns.core.queue;

import java.net.ConnectException;

import javax.jms.JMSException;

/**
 * The Interface IMessageSender.
 *
 * @author hrodriguez
 */
public interface IMessageSender {

  /**
   * Send message.
   *
   * @param message the message
   */
  void sendMessage(String message) throws JMSException, ConnectException;
}
