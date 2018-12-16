/**
 * 
 */
package com.consultec.esigns.core.queue;

/**
 * The Interface HandlerQueue.
 *
 * @author hrodriguez
 */
public interface HandlerQueue {
	
	/**
	 * Handle queue.
	 *
	 * @param raw the raw
	 */
	public void handleQueue(byte[] raw);
}
