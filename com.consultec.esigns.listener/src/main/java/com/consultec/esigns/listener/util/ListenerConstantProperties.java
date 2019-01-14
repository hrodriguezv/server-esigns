
package com.consultec.esigns.listener.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

import com.consultec.esigns.core.security.KeyStoreAccessMode;
import com.consultec.esigns.core.util.InetUtility;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.core.util.SystemCommandExecutor;

/**
 * The Class ListenerConstantProperties.
 *
 * @author hrodriguez
 */
public class ListenerConstantProperties {

	/** The formatter. */
	private static MessageFormat formatter = new MessageFormat(
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_USER_STROKE_REASON));

	/** The Constant urlTSA. */
	public static final String URL_TSA =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.TSA_URL_SERVER);

	/** The Constant loggedUsr. */
	public static final String LOGGED_USER = InetUtility.getLoggedUserNameExt();

	/** The Constant reason. */
	public static final String SIGNATURE_REASON_TEXT = formatter.format(
		new Object[] {
			LOGGED_USER
		});

	/** The Constant keystoreAccessMode. */
	public static final String KEYSTORE_ACCESS_MODE =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.KEYSTORE_ACCESS_MODE);

	/** The Constant PDFVIEWER_EXECPATH. */
	public static final String PDFVIEWER_EXECPATH =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_ICEPDF_PATH);

	/** The Constant PDFVIEWER_EXECDEP. */
	public static final String PDFVIEWER_EXECDEP =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_ICEPDF_DLLS);

	public static final KeyStoreAccessMode configuredKAMode =
		KeyStoreAccessMode.fromString(
			ListenerConstantProperties.KEYSTORE_ACCESS_MODE);

	/**
	 * Instantiates a new listener constant properties.
	 */
	private ListenerConstantProperties() {

	}

	/**
	 * Launch client app.
	 *
	 * @param id
	 *            the id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public static int launchClientApp(String id)
		throws IOException, InterruptedException {

		String[] cmd = {
			"java",
			"-Djava.library.path=\"" +
				ListenerConstantProperties.PDFVIEWER_EXECDEP + "\"",
			"-jar", "icepdf-viewer-6.3.1-SNAPSHOT.jar", "-sessionid", id
		};

		SystemCommandExecutor commandExecutor = new SystemCommandExecutor(
			Arrays.asList(cmd),
			Optional.of(ListenerConstantProperties.PDFVIEWER_EXECPATH));
		int exitValue = commandExecutor.executeCommand();
		if (exitValue != 0) {
			throw new IOException(
				"Error instanciando el visualizador de PDF's");
		}
		return exitValue;
	}

}
