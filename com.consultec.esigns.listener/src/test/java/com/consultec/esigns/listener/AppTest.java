package com.consultec.esigns.listener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.consultec.esigns.listener.Car.Type;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
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
	 * Rigourous Test :-).
	 *
	 * @throws ClassNotFoundException
	 *             the class not found exception
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws SecurityException
	 *             the security exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 * @throws IllegalArgumentException
	 *             the illegal argument exception
	 * @throws InvocationTargetException
	 *             the invocation target exception
	 * @throws IOException
	 */
	public void testApp() throws ClassNotFoundException, NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		String jsonString = "{\"k1\":\"v1\",\"k2\":\"v2\"}";

		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(jsonString);

		JsonFactory factory = mapper.getFactory();
		JsonParser parser = factory.createParser(jsonString);
		@SuppressWarnings("unused")
		JsonNode actualObj2 = mapper.readTree(parser);
		// When
		JsonNode jsonNode1 = actualObj.get("k1");
		assertEquals(jsonNode1.textValue(), "v1");
	}

	public void test() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		Car car = new Car("yellow", Type.SEDAN);
		String carAsString = objectMapper.writeValueAsString(car);
		objectMapper = new ObjectMapper();
		Car carObj = objectMapper.readValue(carAsString, Car.class);
		assertEquals(car.getColor(), carObj.getColor());
	}
}
