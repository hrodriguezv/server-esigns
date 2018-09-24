package com.consultec.esigns.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class WMICUtil.
 */
public class WMICUtil {

	/** The Constant PNP_DEVICE_ID. */
	private static final String PNP_DEVICE_ID = "PNPDeviceID";
	
	/** The Constant DEVICE_ID. */
	private static final String DEVICE_ID = "DeviceID";

	/** The Constant DEFAULT_MONITOR. */
	private static final String DEFAULT_MONITOR = "DesktopMonitor1";

	/** The Constant PNP_REGEX. */
	private static final String PNP_REGEX = ".*?((?:[a-z][a-z]*[0-9]+[a-z0-9]*))";

	/**
	 * Gets the raw properties map.
	 *
	 * @return the raw properties map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static Map<String, List<String>> getRawPropertiesMap() throws IOException {

		String line;
		BufferedReader input = null;
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		String[] cmd = { "cmd", "/c", "wmic", "DESKTOPMONITOR", "list", "full", "/format:csv" };
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			List<String[]> lines = new ArrayList<String[]>();
			while ((line = input.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length > 1)
					lines.add(tokens);
			}

			for (int i = 0; i < lines.size(); i++) {
				int size = lines.get(i).length;
				for (int k = 0; k < size; k++) {
					if (!map.containsKey(lines.get(0)[k])) {
						map.put(lines.get(0)[k], new ArrayList<String>());
					} else {
						map.get(lines.get(0)[k]).add(lines.get(i)[k]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			input.close();
		}
		return map;
	}

	/**
	 * Clean token.
	 *
	 * @param txt the txt
	 * @return the string
	 */
	private static String cleanToken(String txt) {
		Pattern p = Pattern.compile(PNP_REGEX, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
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
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static List<String> getRawDevicesConnected() throws IOException {
		Map<String, List<String>> map = getRawPropertiesMap();
		List<String> temp = map.get(PNP_DEVICE_ID);
		List<String> ret = new ArrayList<>();
		for (String string : temp) {
			if (!string.isEmpty())
			ret.add(cleanToken(string));
		}
		return ret;
	}
	
	/**
	 * Gets the additional devices connected.
	 *
	 * @return the additional devices connected
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static List<String> getAdditionalDevicesConnected() throws IOException {
		Map<String, List<String>> map = getRawPropertiesMap();
		List<String> temp = map.get(PNP_DEVICE_ID);
		List<String> defaultTemp = map.get(DEVICE_ID);
		List<String> ret = new ArrayList<>();
		for (int k = 0; k< temp.size();k++) {
			if (!temp.get(k).isEmpty() && !defaultTemp.get(k).equals(DEFAULT_MONITOR))
			ret.add(cleanToken(temp.get(k)));
		}
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