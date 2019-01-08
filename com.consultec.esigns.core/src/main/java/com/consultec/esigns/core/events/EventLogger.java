package com.consultec.esigns.core.events;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

public class EventLogger {

	private static EventLogger instance;

	/** The mutex. */
	private static Object mutex = new Object();

	private Integer id;

	private final static String appName = "\"Consultec Listener\"";

	private enum Operation {
		INFORMATION, ERROR, WARNING;
	}

	private enum Origin {
		APPLICATION, SYSTEM, SECURITY, INSTALLATION;
	}

	/**
	 * Instantiates a new Event Logger.
	 */
	private EventLogger() {
	}

	private Integer getNextId() {
		synchronized (instance.id) {
			instance.id++;
		}
		return instance.id;
	}

	private static void executeCommand(Operation op, Origin or, String msg) {
		Process process;
		try {
			String command = ("EventCreate /t " + op.name() + " /id " + instance.getNextId() + " /l " + or.name()
					+ " /so " + appName + " /d \"" + msg + "\"");
			System.err.println(command);
			process = Runtime.getRuntime().exec(command);
			process.waitFor(10, TimeUnit.SECONDS);
			int exitValue = process.exitValue();
			System.out.printf("Process exited with value %d\n", exitValue);
		} catch (IOException | InterruptedException e) {
			System.err.println("Error intentando enviar traza al stack de eventos de Windows" + e.getMessage());
		}
	}

	/**
	 * Gets the single instance of Event Logger.
	 *
	 * @return single instance of Event Logger
	 */
	public static EventLogger getInstance() {
		EventLogger result = instance;

		if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new EventLogger();
			}
		}

		return result;
	}

	public void init() {
		String osName = System.getProperty("os.name").toUpperCase(Locale.ENGLISH);
		if (!osName.startsWith("WINDOWS")) {
			throw new RuntimeErrorException(null, "Not Windows");
		}
		Random rand = new Random();
		instance.id = rand.nextInt(1);
	}

	public void error(String msg) {
		executeCommand(Operation.ERROR, Origin.APPLICATION, msg);
	}

	public void info(String msg) {
		executeCommand(Operation.INFORMATION, Origin.APPLICATION, msg);
	}

	public void warn(String msg) {
		executeCommand(Operation.WARNING, Origin.APPLICATION, msg);
	}
}