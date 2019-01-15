
package com.consultec.esigns.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class WMICUtil.
 */
public class WMICUtil {

	/** The Constant logger. */
	private static final Logger logger =
		LoggerFactory.getLogger(WMICUtil.class);

	/** The Constant PNP_DEVICE_ID. */
	private static final String PNP_DEVICE_ID = "PNPDeviceID";

	/** The Constant DEVICE_ID. */
	private static final String DEVICE_ID = "DeviceID";

	/** The Constant DEFAULT_MONITOR. */
	private static final String DEFAULT_MONITOR = "DesktopMonitor1";

	/** The Constant PNP_REGEX. */
	private static final String PNP_REGEX =
		".*?((?:[a-z][a-z]*[0-9]+[a-z0-9]*))";

	/**
	 * Instantiates a new WMIC util.
	 */
	private WMICUtil() {

	}

	/**
	 * Gets the raw properties map.
	 *
	 * @return the raw properties map
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static Map<String, List<String>> getRawPropertiesMap()
		throws IOException {

		Map<String, List<String>> map = new LinkedHashMap<>();
		String[] cmd = {
			"cmd", "/c", "wmic", "DESKTOPMONITOR", "list", "full", "/format:csv"
		};

		try {
			SystemCommandExecutor commandExecutor =
				new SystemCommandExecutor(Arrays.asList(cmd));

			commandExecutor.executeCommand();

			// get the output from the command
			StringBuilder stdout =
				commandExecutor.getStandardOutputFromCommand();
			String[] tokens = stdout.toString().trim().split("\n");
			List<String[]> lines =
				Arrays.asList(tokens).stream().filter(t -> t.length() > 0).map(
					temp -> temp.split(",")).collect(Collectors.toList());

			String[] header = lines.remove(0);
			lines.stream().forEach(
				data -> IntStream.range(0, data.length).forEach(r -> {
					String value = data[r];
					map.compute(header[r], (k, v) -> {
						if (v == null)
							v = new ArrayList<String>();
						v.add(value);
						return v;
					});
				}));
		}
		catch (Exception e) {
			logger.error(
				"Error getting buffer from the given the wmic command", e);
		}
		return map;
	}

	/**
	 * Clean token.
	 *
	 * @param txt
	 *            the txt
	 * @return the string
	 */
	private static String cleanToken(String txt) {

		Pattern p = Pattern.compile(
			PNP_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(txt);
		String alphanum = null;
		if (m.find()) {
			alphanum = m.group(1);
		}
		return alphanum;
	}

	/**
	 * Gets the raw devices connected.
	 *
	 * @return the raw devices connected
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static List<String> getRawDevicesConnected()
		throws IOException {

		Map<String, List<String>> map = getRawPropertiesMap();
		List<String> temp = map.get(PNP_DEVICE_ID);
		return temp.stream().filter(string -> !string.isEmpty()).map(
			WMICUtil::cleanToken).collect(Collectors.toList());
	}

	/**
	 * Gets the additional devices connected.
	 *
	 * @return the additional devices connected
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static List<String> getAdditionalDevicesConnected()
		throws IOException {

		Map<String, List<String>> map = getRawPropertiesMap();
		List<String> temp = map.get(PNP_DEVICE_ID);
		List<String> defaultTemp = map.get(DEVICE_ID);
		List<String> ret = new ArrayList<>();
		IntStream.range(0, temp.size()).forEach(k -> {
			if (!temp.get(k).isEmpty() &&
				!defaultTemp.get(k).equals(DEFAULT_MONITOR))
				ret.add(cleanToken(temp.get(k)));
		});

		return ret;
	}

	/**
	 * Gets the logged user.
	 *
	 * @return the logged user
	 */
	public static String getLoggedUser() {

		return System.getProperty("user.name");
	}
}
