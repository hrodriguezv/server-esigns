
package com.consultec.esigns.core.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class PropertiesManager.
 */
public class PropertiesManager {

	/** The Constant logger. */
	private static final Logger logger =
		Logger.getLogger(PropertiesManager.class.toString());

	/** The Constant PROPERTY_TOKEN_SEPARATOR. */
	// use ascii '27' or ESC as the delimiting character when storing multiple
	// values in one property name.
	public static final String PROPERTY_TOKEN_SEPARATOR = "|";

	/** The default prop file. */
	// default file for all not specified properties
	public static final String DEFAULT_PROP_FILE = "application.properties";

	/** The default prop file path. */
	public static final String DEFAULT_PROP_FILE_PATH = "";

	/** The default message bundle. */
	public static final String DEFAULT_MESSAGE_BUNDLE =
		"com.consultec.esigns.resources.MessageBundle";

	/** The properties manager. */
	private static PropertiesManager propertiesManager;

	/** The default props. */
	// default properties file included int the viewer jar
	private static Properties defaultProps;

	/** The Constant PROPERTY_DEFAULT_FILE_PATH. */
	public static final String PROPERTY_DEFAULT_FILE_PATH =
		"application.default.filepath";

	/** The Constant PROPERTY_USER_HOME_WORKSPACE. */
	public static final String PROPERTY_USER_HOME_WORKSPACE =
		"user.default.home";

	/** The Constant PROPERTY_USER_HOME_PDFDOCUMENT. */
	public static final String PROPERTY_USER_HOME_PDFDOCUMENT =
		"user.default.pdfdocumentname";

	/** The Constant PROPERTY_USER_HOME_ESIGNEDDOCUMENT. */
	public static final String PROPERTY_USER_HOME_ESIGNEDDOCUMENT =
		"user.default.pdfesigneddocname";

	/** The Constant PROPERTY_USER_HOME_STROKEDOCUMENT. */
	public static final String PROPERTY_USER_HOME_STROKEDOCUMENT =
		"user.default.pdfstrokedocname";

	/** The Constant PROPERTY_USER_BASE_HOME. */
	public static final String PROPERTY_USER_BASE_HOME =
		"user.default.basehome";

	/** The Constant PROPERTY_OPERATOR_CERTIFICATE. */
	public static final String PROPERTY_OPERATOR_CERTIFICATE =
		"operator.default.certificate";
	public static final String KEY_OPERATOR_CERTIFICATE =
		"operator.password.certificate";

	/** The Constant QUEUE_SERVER_NAME. */
	public static final String QUEUE_SERVER_NAME = "apache.activeq.servername";

	/** The Constant QUEUE_SERVER_HOST. */
	public static final String QUEUE_SERVER_HOST = "apache.activeq.host";

	/** The Constant QUEUE_SERVER_PORT. */
	public static final String QUEUE_SERVER_PORT = "apache.activeq.port";

	/** The Constant TSA_URL_SERVER. */
	public static final String TSA_URL_SERVER = "tsa.server.url";

	/** The Constant PROPERTY_ICEPDF_PATH. */
	public static final String PROPERTY_ICEPDF_PATH =
		"user.default.icepdfjarpath";

	/** The Constant PROPERTY_ICEPDF_DLLS. */
	public static final String PROPERTY_ICEPDF_DLLS =
		"user.default.icepdfdllpath";

	/** The Constant PROPERTY_USER_STROKE_IMGEXT. */
	public static final String PROPERTY_USER_STROKE_IMGEXT =
		"user.default.strokeimgextension";

	/** The Constant PROPERTY_USER_STROKE_TEXTEXT. */
	public static final String PROPERTY_USER_STROKE_TEXTEXT =
		"user.default.stroketxtextension";

	/** The Constant PROPERTY_USER_STROKE_REASON. */
	public static final String PROPERTY_USER_STROKE_REASON =
		"stroke.reason.value";

	public static final String PROPERTY_USER_STROKE_LOCATION =
		"stroke.location.value";

	/** The Constant KEYSTORE_ACCESS_MODE. */
	public static final String KEYSTORE_ACCESS_MODE = "keystore.access.type";

	/** The Constant PROPERTY_USER_STROKE_IMGNAME. */
	public static final String PROPERTY_USER_STROKE_FILENAME =
		"user.default.strokefilebasename";

	/** The Constant DEFAULT_FORMATTER_MASK. */
	public static final String DEFAULT_FORMATTER_MASK =
		"stroke.dateformatter.timestamp";

	public static final String DELETE_DATA_ON_EXIT =
		"stroke.delete.data.onexit";

	/** The mutex. */
	private static Object mutex = new Object();

	/**
	 * Instantiates a new properties manager.
	 */
	private PropertiesManager() {

	}

	/**
	 * Gets singleton instance of the the Properties manager instance.
	 *
	 * @return singleton instance.
	 */
	public static PropertiesManager getInstance() {

		PropertiesManager result = propertiesManager;
		if (result == null) {
			synchronized (mutex) {
				result = propertiesManager;
				if (result == null)
					propertiesManager = new PropertiesManager();
				setupDefaultProperties();
			}
		}
		return propertiesManager;
	}

	/**
	 * Reads the properties file that ships with the viewer jar and stores the
	 * default properties in the preferences backing store. This is only done on
	 * first launch.
	 */
	private static void setupDefaultProperties() {

		ClassLoader classLoader =
			Thread.currentThread().getContextClassLoader();
		try (InputStream in = classLoader.getResourceAsStream(
			DEFAULT_PROP_FILE_PATH + DEFAULT_PROP_FILE)) {
			defaultProps = new Properties();
			if (in != null) {
				defaultProps.load(in);
			}
			else {
				throw new FileNotFoundException(
					"Error loading default properties file");
			}
		}
		catch (Exception ex) {
			// log the error
			if (logger.isLoggable(Level.WARNING)) {
				logger.log(
					Level.WARNING, "Error loading default properties cache",
					ex);
			}
		}
	}

	/**
	 * Gets the value.
	 *
	 * @param key
	 *            the key
	 * @return the value
	 */
	public String getValue(String key) {

		return defaultProps.getProperty(key);
	}
}
