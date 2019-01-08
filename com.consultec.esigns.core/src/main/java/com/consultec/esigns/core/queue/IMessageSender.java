/**
 * 
 */
package com.consultec.esigns.core.queue;

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
	void sendMessage(String message);
}
