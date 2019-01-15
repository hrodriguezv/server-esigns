
package com.consultec.esigns.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is intended to be used with the SystemCommandExecutor class to let
 * users execute system commands from Java applications.
 */
class ThreadedStreamHandler extends Thread {

	/** The Constant logger. */
	private static final Logger logger =
		LoggerFactory.getLogger(ThreadedStreamHandler.class);

	/** The input stream. */
	InputStream inputStream;

	/** The admin password. */
	String adminPassword;

	/** The output stream. */
	OutputStream outputStream;

	/** The print writer. */
	PrintWriter printWriter;

	/** The output buffer. */
	StringBuilder outputBuffer = new StringBuilder();

	/** The sudo is requested. */
	private boolean sudoIsRequested = false;

	/**
	 * A simple constructor for when the sudo command is not necessary. This
	 * constructor will just run the command you provide, without running sudo
	 * before the command, and without expecting a password.
	 *
	 * @param inputStream
	 *            the input stream
	 */
	ThreadedStreamHandler(InputStream inputStream) {

		this.inputStream = inputStream;
	}

	/**
	 * Use this constructor when you want to invoke the 'sudo' command. The
	 * outputStream must not be null. If it is, you'll regret it. :) this
	 * currently hangs if the admin password given for the sudo command is
	 * wrong.
	 *
	 * @param inputStream
	 *            the input stream
	 * @param outputStream
	 *            the output stream
	 * @param adminPassword
	 *            the admin password
	 */
	ThreadedStreamHandler(
		InputStream inputStream, OutputStream outputStream,
		String adminPassword) {

		this.inputStream = inputStream;
		this.outputStream = outputStream;
		this.printWriter = new PrintWriter(outputStream);
		this.adminPassword = adminPassword;
		this.sudoIsRequested = true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {

		// on mac os x 10.5.x, when i run a 'sudo' command, i need to write
		// the admin password out immediately; that's why this code is
		// here.
		if (sudoIsRequested) {
			printWriter.println(adminPassword);
			printWriter.flush();
		}

		try (BufferedReader bufferedReader =
			new BufferedReader(new InputStreamReader(inputStream))) {
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				outputBuffer.append(line + "\n");
			}
		}
		catch (IOException ioe) {
			logger.error("Error getting the buffer given the command", ioe);
		}
		catch (Exception t) {
			logger.error("Error getting the buffer given the command", t);
		}
	}

	/**
	 * Gets the output buffer.
	 *
	 * @return the output buffer
	 */
	public StringBuilder getOutputBuffer() {

		return outputBuffer;
	}

}
