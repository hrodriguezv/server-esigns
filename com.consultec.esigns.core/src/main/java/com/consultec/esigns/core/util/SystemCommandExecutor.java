
package com.consultec.esigns.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

/**
 * This class can be used to execute a system command from a Java application.
 */
public class SystemCommandExecutor {

	/** The command information. */
	private List<String> commandInformation;

	/** The admin password. */
	private String adminPassword;

	/** The input stream handler. */
	private ThreadedStreamHandler inputStreamHandler;

	/** The error stream handler. */
	private ThreadedStreamHandler errorStreamHandler;

	/** The directory. */
	private Optional<String> directory;

	/**
	 * Pass in the system command you want to run as a List of Strings.
	 *
	 * @param commandInformation
	 *            The command you want to run.
	 */
	public SystemCommandExecutor(final List<String> commandInformation) {

		if (commandInformation == null)
			throw new NullPointerException(
				"The commandInformation is required.");
		this.commandInformation = commandInformation;
		this.adminPassword = null;
		this.directory = Optional.ofNullable(null);
	}

	/**
	 * Instantiates a new system command executor.
	 *
	 * @param commandInformation
	 *            the command information
	 * @param dir
	 *            the dir
	 */
	public SystemCommandExecutor(
		final List<String> commandInformation, Optional<String> dir) {

		this(commandInformation);
		this.directory = dir;
	}

	/**
	 * Execute command.
	 *
	 * @return the int
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public int executeCommand()
		throws IOException, InterruptedException {

		ProcessBuilder pb = new ProcessBuilder(commandInformation);

		if (this.directory.isPresent()) {
			pb.directory(new File(directory.get()));
		}

		Process process = pb.start();

		OutputStream stdOutput = process.getOutputStream();

		InputStream inputStream = process.getInputStream();
		InputStream errorStream = process.getErrorStream();

		inputStreamHandler =
			new ThreadedStreamHandler(inputStream, stdOutput, adminPassword);
		errorStreamHandler = new ThreadedStreamHandler(errorStream);

		inputStreamHandler.start();
		errorStreamHandler.start();

		int exitValue = process.waitFor();

		inputStreamHandler.interrupt();
		errorStreamHandler.interrupt();
		inputStreamHandler.join();
		errorStreamHandler.join();

		return exitValue;
	}

	/**
	 * Get the standard output (stdout) from the command you just exec'd.
	 *
	 * @return the standard output from command
	 */
	public StringBuilder getStandardOutputFromCommand() {

		return inputStreamHandler.getOutputBuffer();
	}

	/**
	 * Get the standard error (stderr) from the command you just exec'd.
	 *
	 * @return the standard error from command
	 */
	public StringBuilder getStandardErrorFromCommand() {

		return errorStreamHandler.getOutputBuffer();
	}

}
