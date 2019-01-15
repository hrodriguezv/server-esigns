/**
 * 
 */

package com.consultec.esigns.listener.lang;

import com.consultec.esigns.core.lang.GeneralGenericException;

/**
 * @author hrodriguez
 */
public class GeneralErrorListenerException extends GeneralGenericException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7813302326426408080L;

	/**
	 * 
	 */
	public GeneralErrorListenerException() {

	}

	/**
	 * @param arg0
	 */
	public GeneralErrorListenerException(String arg0) {

		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public GeneralErrorListenerException(Throwable arg0) {

		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public GeneralErrorListenerException(String arg0, Throwable arg1) {

		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public GeneralErrorListenerException(
		String arg0, Throwable arg1, boolean arg2, boolean arg3) {

		super(arg0, arg1, arg2, arg3);
	}

}
