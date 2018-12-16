/**
 * 
 */
package com.consultec.esigns.core.queue;

/**
 * The Interface IMessageHandler.
 *
 * @author hrodriguez
 */
public interface IMessageHandler {
	
	/**
	 * Process msg.
	 *
	 * @param msg the msg
	 */
	public void processMsg(String msg);
}
