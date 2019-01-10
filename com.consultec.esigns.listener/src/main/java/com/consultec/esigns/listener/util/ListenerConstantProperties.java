
package com.consultec.esigns.listener.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consultec.esigns.core.util.InetUtility;
import com.consultec.esigns.core.util.PropertiesManager;

/**
 * The Class ListenerConstantProperties.
 *
 * @author hrodriguez
 */
public class ListenerConstantProperties {

	/** The Constant logger. */
	private static final Logger logger =
		LoggerFactory.getLogger(ListenerConstantProperties.class);

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

	/**
	 * Launch client app.
	 *
	 * @param id
	 *            the id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void launchClientApp(String id)
		throws IOException {

		String cmmd = ("java -Djava.library.path=\"" +
			ListenerConstantProperties.PDFVIEWER_EXECDEP + "\"" +
			" -jar icepdf-viewer-6.3.1-SNAPSHOT.jar -sessionid " + id);
		System.err.println(
			ListenerConstantProperties.PDFVIEWER_EXECPATH + " " + cmmd);
		BufferedReader input = null;

		try {
			Process p = Runtime.getRuntime().exec(
				cmmd, null,
				new File(ListenerConstantProperties.PDFVIEWER_EXECPATH));
			input =
				new BufferedReader(new InputStreamReader(p.getErrorStream()));
		}
		catch (Exception e) {
			logger.error(
				"There was an error trying to launch icepdf viewer ", e);
			e.printStackTrace();
		}
		finally {
			input.close();
		}
	}

}
