package com.consultec.esigns.core.lang;

/**
 * @author hrodriguez
 */
public class GeneralGenericException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6209680949812727249L;

	/**
	 * 
	 */
	public GeneralGenericException() {

	}

	/**
	 * @param arg0
	 */
	public GeneralGenericException(String arg0) {

		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public GeneralGenericException(Throwable arg0) {

		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public GeneralGenericException(String arg0, Throwable arg1) {

		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public GeneralGenericException(
		String arg0, Throwable arg1, boolean arg2, boolean arg3) {

		super(arg0, arg1, arg2, arg3);
	}
}
