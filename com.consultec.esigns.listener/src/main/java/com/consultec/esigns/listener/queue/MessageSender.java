package com.consultec.esigns.listener.queue;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.consultec.esigns.core.queue.IMessageSender;
import com.consultec.esigns.core.util.PropertiesManager;

import lombok.extern.slf4j.Slf4j;

/**
 * The Class MessageSender.
 */
@Slf4j
@Component("MessageSender")
public class MessageSender implements IMessageSender {

  /** The connection factory. */
  @Autowired
  private ConnectionFactory connectionFactory;

  /** The jms template. */
  private JmsTemplate jmsTemplate;

  /**
   * Inits the.
   */
  @PostConstruct
  public void init() {

    this.jmsTemplate = new JmsTemplate(connectionFactory);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.core.queue.IMessageSender#sendMessage(java.lang.String)
   */
  public void sendMessage(final String message) {

    log.debug("Sending: {}", message);

    jmsTemplate.send(PropertiesManager.getInstance().getValue(PropertiesManager.QUEUE_SERVER_NAME),
      new MessageCreator() {
        public Message createMessage(Session session) throws JMSException {
          return session.createTextMessage(message);
        }
      });

  }

}
