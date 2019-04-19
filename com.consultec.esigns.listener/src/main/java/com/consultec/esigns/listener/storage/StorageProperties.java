
package com.consultec.esigns.listener.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.consultec.esigns.core.util.PropertiesManager;

@ConfigurationProperties("storage")
public class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	// private String location = "upload-dir";
	private String location = PropertiesManager.getInstance().getValue(
		PropertiesManager.PROPERTY_USER_BASE_HOME);

	public String getLocation() {

		return location;
	}

	public void setLocation(String location) {

		this.location = location;
	}

}
