package com.consultec.esigns.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

// TODO: Auto-generated Javadoc
/**
 * The Class WinRegistry.
 */
public class WinRegistry {
	
	/** The Constant HKEY_CURRENT_USER. */
	public static final int HKEY_CURRENT_USER = 0x80000001;
	
	/** The Constant HKEY_LOCAL_MACHINE. */
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	
	/** The Constant REG_SUCCESS. */
	public static final int REG_SUCCESS = 0;
	
	/** The Constant REG_NOTFOUND. */
	public static final int REG_NOTFOUND = 2;
	
	/** The Constant REG_ACCESSDENIED. */
	public static final int REG_ACCESSDENIED = 5;

	/** The Constant KEY_ALL_ACCESS. */
	private static final int KEY_ALL_ACCESS = 0xf003f;
	
	/** The Constant KEY_READ. */
	private static final int KEY_READ = 0x20019;
	
	/** The Constant userRoot. */
	private static final Preferences userRoot = Preferences.userRoot();
	
	/** The Constant systemRoot. */
	private static final Preferences systemRoot = Preferences.systemRoot();
	
	/** The Constant userClass. */
	private static final Class<? extends Preferences> userClass = userRoot.getClass();
	
	/** The Constant regOpenKey. */
	private static final Method regOpenKey;
	
	/** The Constant regCloseKey. */
	private static final Method regCloseKey;
	
	/** The Constant regQueryValueEx. */
	private static final Method regQueryValueEx;
	
	/** The Constant regEnumValue. */
	private static final Method regEnumValue;
	
	/** The Constant regQueryInfoKey. */
	private static final Method regQueryInfoKey;
	
	/** The Constant regEnumKeyEx. */
	private static final Method regEnumKeyEx;
	
	/** The Constant regCreateKeyEx. */
	private static final Method regCreateKeyEx;
	
	/** The Constant regSetValueEx. */
	private static final Method regSetValueEx;
	
	/** The Constant regDeleteKey. */
	private static final Method regDeleteKey;
	
	/** The Constant regDeleteValue. */
	private static final Method regDeleteValue;

	static {
		try {
			regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey",
					new Class[] { int.class, byte[].class, int.class });
			regOpenKey.setAccessible(true);
			regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[] { int.class });
			regCloseKey.setAccessible(true);
			regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx",
					new Class[] { int.class, byte[].class });
			regQueryValueEx.setAccessible(true);
			regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue",
					new Class[] { int.class, int.class, int.class });
			regEnumValue.setAccessible(true);
			regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", new Class[] { int.class });
			regQueryInfoKey.setAccessible(true);
			regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx",
					new Class[] { int.class, int.class, int.class });
			regEnumKeyEx.setAccessible(true);
			regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx",
					new Class[] { int.class, byte[].class });
			regCreateKeyEx.setAccessible(true);
			regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx",
					new Class[] { int.class, byte[].class, byte[].class });
			regSetValueEx.setAccessible(true);
			regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue",
					new Class[] { int.class, byte[].class });
			regDeleteValue.setAccessible(true);
			regDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", new Class[] { int.class, byte[].class });
			regDeleteKey.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Instantiates a new win registry.
	 */
	private WinRegistry() {
	}

	/**
	 * Read a value from key and value name.
	 *
	 * @param hkey            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key the key
	 * @param valueName the value name
	 * @return the value
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static String readString(int hkey, String key, String valueName)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readString(systemRoot, hkey, key, valueName);
		} else if (hkey == HKEY_CURRENT_USER) {
			return readString(userRoot, hkey, key, valueName);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Read value(s) and value name(s) form given key.
	 *
	 * @param hkey            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key the key
	 * @return the value name(s) plus the value(s)
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static Map<String, String> readStringValues(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readStringValues(systemRoot, hkey, key);
		} else if (hkey == HKEY_CURRENT_USER) {
			return readStringValues(userRoot, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Read the value name(s) from a given key.
	 *
	 * @param hkey            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key the key
	 * @return the value name(s)
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static List<String> readStringSubKeys(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readStringSubKeys(systemRoot, hkey, key);
		} else if (hkey == HKEY_CURRENT_USER) {
			return readStringSubKeys(userRoot, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Create a key.
	 *
	 * @param hkey            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key the key
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void createKey(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] ret;
		if (hkey == HKEY_LOCAL_MACHINE) {
			ret = createKey(systemRoot, hkey, key);
			regCloseKey.invoke(systemRoot, new Object[] { new Integer(ret[0]) });
		} else if (hkey == HKEY_CURRENT_USER) {
			ret = createKey(userRoot, hkey, key);
			regCloseKey.invoke(userRoot, new Object[] { new Integer(ret[0]) });
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
		if (ret[1] != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
		}
	}

	/**
	 * Write a value in a given key/value name.
	 *
	 * @param hkey the hkey
	 * @param key the key
	 * @param valueName the value name
	 * @param value the value
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void writeStringValue(int hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			writeStringValue(systemRoot, hkey, key, valueName, value);
		} else if (hkey == HKEY_CURRENT_USER) {
			writeStringValue(userRoot, hkey, key, valueName, value);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Delete a given key.
	 *
	 * @param hkey the hkey
	 * @param key the key
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void deleteKey(int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc = -1;
		if (hkey == HKEY_LOCAL_MACHINE) {
			rc = deleteKey(systemRoot, hkey, key);
		} else if (hkey == HKEY_CURRENT_USER) {
			rc = deleteKey(userRoot, hkey, key);
		}
		if (rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
		}
	}

	/**
	 * delete a value from a given key/value name.
	 *
	 * @param hkey the hkey
	 * @param key the key
	 * @param value the value
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void deleteValue(int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc = -1;
		if (hkey == HKEY_LOCAL_MACHINE) {
			rc = deleteValue(systemRoot, hkey, key, value);
		} else if (hkey == HKEY_CURRENT_USER) {
			rc = deleteValue(userRoot, hkey, key, value);
		}
		if (rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
		}
	}

	// =====================

	/**
	 * Delete value.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @param value the value
	 * @return the int
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static int deleteValue(Preferences root, int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] handles = (int[]) regOpenKey.invoke(root,
				new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });
		if (handles[1] != REG_SUCCESS) {
			return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED
		}
		int rc = ((Integer) regDeleteValue.invoke(root, new Object[] { new Integer(handles[0]), toCstr(value) }))
				.intValue();
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		return rc;
	}

	/**
	 * Delete key.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @return the int
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static int deleteKey(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc = ((Integer) regDeleteKey.invoke(root, new Object[] { new Integer(hkey), toCstr(key) })).intValue();
		return rc; // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
	}

	/**
	 * Read string.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @param value the value
	 * @return the string
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static String readString(Preferences root, int hkey, String key, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] handles = (int[]) regOpenKey.invoke(root,
				new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		byte[] valb = (byte[]) regQueryValueEx.invoke(root, new Object[] { new Integer(handles[0]), toCstr(value) });
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		return (valb != null ? new String(valb).trim() : null);
	}

	/**
	 * Read string values.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @return the map
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static Map<String, String> readStringValues(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		HashMap<String, String> results = new HashMap<String, String>();
		int[] handles = (int[]) regOpenKey.invoke(root,
				new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] { new Integer(handles[0]) });

		int count = info[0]; // count
		int maxlen = info[3]; // value length max
		for (int index = 0; index < count; index++) {
			byte[] name = (byte[]) regEnumValue.invoke(root,
					new Object[] { new Integer(handles[0]), new Integer(index), new Integer(maxlen + 1) });
			if (name != null) {
				String value = readString(hkey, key, new String(name));
				results.put(new String(name).trim(), value);
			}
		}
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		return results;
	}

	/**
	 * Read string sub keys.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @return the list
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static List<String> readStringSubKeys(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		List<String> results = new ArrayList<String>();
		int[] handles = (int[]) regOpenKey.invoke(root,
				new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_READ) });
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		int[] info = (int[]) regQueryInfoKey.invoke(root, new Object[] { new Integer(handles[0]) });

		int count = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj,
								// confirmed by Petrucio
		int maxlen = info[3]; // value length max
		for (int index = 0; index < count; index++) {
			byte[] name = (byte[]) regEnumKeyEx.invoke(root,
					new Object[] { new Integer(handles[0]), new Integer(index), new Integer(maxlen + 1) });
			results.add(new String(name).trim());
		}
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
		return results;
	}

	/**
	 * Creates the key.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @return the int[]
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static int[] createKey(Preferences root, int hkey, String key)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return (int[]) regCreateKeyEx.invoke(root, new Object[] { new Integer(hkey), toCstr(key) });
	}

	/**
	 * Write string value.
	 *
	 * @param root the root
	 * @param hkey the hkey
	 * @param key the key
	 * @param valueName the value name
	 * @param value the value
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] handles = (int[]) regOpenKey.invoke(root,
				new Object[] { new Integer(hkey), toCstr(key), new Integer(KEY_ALL_ACCESS) });

		regSetValueEx.invoke(root, new Object[] { new Integer(handles[0]), toCstr(valueName), toCstr(value) });
		regCloseKey.invoke(root, new Object[] { new Integer(handles[0]) });
	}

	/**
	 * To cstr.
	 *
	 * @param str the str
	 * @return the byte[]
	 */
	// utility
	private static byte[] toCstr(String str) {
		byte[] result = new byte[str.length() + 1];

		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}
}