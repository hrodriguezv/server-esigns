/**
 * 
 */

package com.consultec.esigns.core.util;

import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.Secur32Util;

/**
 * The Class InetUtility.
 *
 * @author hrodriguez
 */
public class InetUtility {

	private static final String LOCALHOST = "localhost";

	/**
	 * Checks if a server is reachable.
	 *
	 * @param serverUrl
	 *            the server url
	 * @return true, if is reachable
	 */
	public static boolean isReachable(String serverUrl) {

		final Socket socket;

		try {
			URL url = new URL(serverUrl);
			socket = new Socket(
				url.getHost(), (url.getPort() < 0 ? 80 : url.getPort()));
		}
		catch (Throwable e) {
			return false;
		}

		try {
			socket.close();
		}
		catch (Exception e) {
		}
		return true;
	}

	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
	public static String getHostName() {

		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostName();
		}
		catch (UnknownHostException e) {
		}
		return LOCALHOST;
	}

	/**
	 * Gets the logged user name ext.
	 *
	 * @return the logged user name ext
	 */
	public static String getLoggedUserNameExt() {

		String fullName = null;
		try {
			fullName = Secur32Util.getUserNameEx(
				Secur32.EXTENDED_NAME_FORMAT.NameDisplay);
		}
		catch (Exception e) {
		}
		return fullName;
	}
}
