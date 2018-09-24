package com.consultec.esigns.core;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import com.consultec.esigns.core.util.WinRegistry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// TODO: Auto-generated Javadoc
/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
	
	/**
	 * Create the test case.
	 *
	 * @param testName            name of the test case
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
	 * Rigourous Test :-).
	 */
	public void app() {
		Properties props = System.getProperties();
		props.list(System.out);
		assertTrue(true);
	}

	/**
	 * Graphics.
	 */
	public void graphics() {

		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice device : environment.getScreenDevices()) {
			System.out.println(device);
			System.out.println("\t" + device.getIDstring());
			System.out.println("\t" + device.getType());
		}

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] sds = ge.getScreenDevices();
		for (GraphicsDevice sd : sds) {
			System.err.println(sd.getDisplayMode().getClass().getName());
			;
			System.err.println(sd.getIDstring());

			System.err.println(sd.getClass().getSimpleName());
			;
			System.err.println(sd.getDefaultConfiguration().getBufferCapabilities().getFrontBufferCapabilities()
					.getClass().getSimpleName());
			GraphicsConfiguration gc = sd.getDefaultConfiguration();
			System.err.println(gc.getClass().getSimpleName());
			;
		}
	}

	/**
	 * Test registry.
	 */
	public void testRegistry() {

		String value;
		try {
			value = WinRegistry.readString(WinRegistry.HKEY_LOCAL_MACHINE, // HKEY
					"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", // Key
					"ProductName");
			System.out.println("Windows Distribution = " + value);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		} // ValueName

		Map<String, String> map;
		try {
			map = WinRegistry.readStringValues(WinRegistry.HKEY_LOCAL_MACHINE,
					"SYSTEM\\CurrentControlSet\\Enum\\DISPLAY");
			for (Entry<String, String> key : map.entrySet()) {
				System.err.println(key.getKey() + " - " + key.getValue());
			}

		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // HKEY

		try {
			map = WinRegistry.readStringValues(WinRegistry.HKEY_LOCAL_MACHINE,
					"SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion");
			for (Entry<String, String> key : map.entrySet()) {
				System.err.println(key.getKey() + " - " + key.getValue());
			}

		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // HKEY

	}
}
