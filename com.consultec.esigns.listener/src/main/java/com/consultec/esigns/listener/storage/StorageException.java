package com.consultec.esigns.listener.storage;

public class StorageException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4794854198260634101L;

	public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
