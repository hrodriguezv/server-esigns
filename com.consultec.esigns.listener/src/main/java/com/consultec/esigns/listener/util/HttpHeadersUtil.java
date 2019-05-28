/**
 * 
 */
package com.consultec.esigns.listener.util;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.http.HttpHeaders;

/**
 * @author alexivrodriguez
 *
 */
public class HttpHeadersUtil {

	public static String getValueFromHeaderKey(HttpHeaders headers, String key) {
		Set<Entry<String, List<String>>> set = headers.entrySet();
		Optional<Entry<String, List<String>>> value = set.stream().filter(entry -> entry.getKey().equals(key))
				.findFirst();
		if (value.isPresent()) {
			Optional<String> optionalKey = value.get().getValue().stream().findFirst();
			if (optionalKey.isPresent()) {
				return optionalKey.get();
			}
		}
		return null;
	}
}
