
package com.consultec.esigns.core;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consultec.esigns.core.util.WinRegistry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

	private static final String ERROR_TESTING_MSG = "Error testing :";

	/**
	 * Create the test case.
	 *
	 * @param testName
	 *            name of the test case
	 */
	public AppTest(String testName) {

		super(testName);
	}

	/**
	 * Suite.
	 *
	 * @return the suite of tests being tested
	 */
	public static Test suite() {

		return new TestSuite(AppTest.class);
	}

	/**
	 * Graphics.
	 */
	public void graphics() {

		final GraphicsEnvironment environment =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice device : environment.getScreenDevices()) {
			logger.info("\t" + device.toString());
			logger.info("\t" + device.getIDstring());
			logger.info("\t" + device.getType());
		}

		GraphicsEnvironment ge =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] sds = ge.getScreenDevices();
		for (GraphicsDevice sd : sds) {
			logger.info(sd.getDisplayMode().getClass().getName());
			logger.info(sd.getIDstring());
			logger.info(sd.getClass().getSimpleName());
			logger.info(
				sd.getDefaultConfiguration().getBufferCapabilities().getFrontBufferCapabilities().getClass().getSimpleName());
			GraphicsConfiguration gc = sd.getDefaultConfiguration();
			logger.info(gc.getClass().getSimpleName());
		}
	}

	/**
	 * Test registry.
	 */
	public void testRegistry() {

		String value;
		try {
			value = WinRegistry.readString(
				WinRegistry.HKEY_LOCAL_MACHINE, // HKEY
				"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", // Key
				"ProductName");
			logger.info("Windows Distribution = " + value);
		}
		catch (IllegalArgumentException | IllegalAccessException
						| InvocationTargetException e) {
			logger.error(ERROR_TESTING_MSG + e.getMessage());
		} // ValueName

		Map<String, String> map;
		try {
			map = WinRegistry.readStringValues(
				WinRegistry.HKEY_LOCAL_MACHINE,
				"SYSTEM\\CurrentControlSet\\Enum\\DISPLAY");
			for (Entry<String, String> key : map.entrySet()) {
				logger.info(key.getKey() + " - " + key.getValue());
			}

		}
		catch (IllegalArgumentException | IllegalAccessException
						| InvocationTargetException e) {
			logger.error(ERROR_TESTING_MSG + e.getMessage());
		} // HKEY

		try {
			map = WinRegistry.readStringValues(
				WinRegistry.HKEY_LOCAL_MACHINE,
				"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
			for (Entry<String, String> key : map.entrySet()) {
				logger.info(key.getKey() + " - " + key.getValue());
			}

		}
		catch (IllegalArgumentException | IllegalAccessException
						| InvocationTargetException e) {
			logger.error(ERROR_TESTING_MSG + e.getMessage());
		} // HKEY

	}
}
