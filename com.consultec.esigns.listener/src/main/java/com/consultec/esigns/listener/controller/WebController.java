package com.consultec.esigns.listener.controller;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.listener.config.QueueConfig;
import com.consultec.esigns.listener.queue.MessageSender;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class WebController.
 */
@RestController
@RequestMapping("/files") // RequestMapping for Class
public class WebController {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(WebController.class);

	/**
	 * Gets the value from header key.
	 *
	 * @param headers the headers
	 * @param key the key
	 * @return the value from header key
	 */
	private String getValueFromHeaderKey(HttpHeaders headers, String key) {
		Set<Entry<String, List<String>>> set = headers.entrySet();
		Optional<Entry<String,List<String>>> value = set.stream().filter(entry -> entry.getKey().equals(key)).findFirst();
		if (value.isPresent()) {
			Optional<String> optionalKey = value.get().getValue().stream()
			.findFirst();
			if (optionalKey.isPresent()) {
				return optionalKey.get();
			}
		}
		return null;
	}
	
	/**
	 * Do work.
	 *
	 * @param pobj the pobj
	 */
	private void doWork(PayloadTO pobj) {
		try {
			// serialize and send package to queue in form of a json object
			ObjectMapper objectMapper = new ObjectMapper();
			String pckg = objectMapper.writeValueAsString(pobj);

			MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);
		} catch (Exception e) {
			logger.error("Se produjo un error intentando enviar el paquete a la cola", e);
		}
	}

	/**
	 * Receive.
	 *
	 * @param p1 the p 1
	 * @param headers the headers
	 */
	@RequestMapping(value = "/receive", method = { RequestMethod.GET, RequestMethod.POST }, consumes = {
			"application/json" })
	public void receive(@RequestBody PayloadTO p1, @RequestHeader HttpHeaders headers) {
		p1.setSessionID(getValueFromHeaderKey(headers, "ngsesid"));
		p1.setOrigin(getValueFromHeaderKey(headers, "origin"));
		p1.setSerializedObj(headers);
		doWork(p1);
	}
}
