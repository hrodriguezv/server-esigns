
package com.consultec.esigns.core.lang;

/**
 * @author hrodriguez
 */
public class GeneralGenericError extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4294729189073910652L;

	/**
	 * 
	 */
	public GeneralGenericError() {

	}

	/**
	 * @param arg0
	 */
	public GeneralGenericError(String arg0) {

		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public GeneralGenericError(Throwable arg0) {

		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public GeneralGenericError(String arg0, Throwable arg1) {

		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public GeneralGenericError(
		String arg0, Throwable arg1, boolean arg2, boolean arg3) {

		super(arg0, arg1, arg2, arg3);
	}
}
