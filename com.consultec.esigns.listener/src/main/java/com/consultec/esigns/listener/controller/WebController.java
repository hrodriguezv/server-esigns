package com.consultec.esigns.listener.controller;

import static com.consultec.esigns.listener.util.HttpHeadersUtil.getValueFromHeaderKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
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

	private void receiveFile(PayloadTO pobj) {
		try {
			// serialize and send package to queue in form of a json object
			ObjectMapper objectMapper = new ObjectMapper();
			String pckg = objectMapper.writeValueAsString(pobj);

			MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);
		} catch (Exception e) {
			logger.error("Se produjo un error intentando enviar el paquete a la cola", e);
		}
	}

	private void handleFileUpload(PayloadTO pobj) {
		String sessionId;
		try {
			pobj.setStage(Stage.MANUAL_SIGNED);
			sessionId = pobj.getSessionID();

			FileSystemManager manager = FileSystemManager.getInstance();

			manager.init(sessionId);
			manager.createLocalWorkspace(pobj.getStage(), sessionId, pobj.getPlainDocEncoded());

			manager.serializeObjectFile(pobj);
			// storageService.store(file);

			ObjectMapper objectMapper = new ObjectMapper();
			String pckg = objectMapper.writeValueAsString(pobj);

			MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);

		} catch (Exception e) {
			logger.error("Se produjo un error intentando enviar el paquete a la cola", e);
		}
	}

	private PayloadTO setDefaultValuesToWrapper(PayloadTO p1, HttpHeaders headers) {
		p1.setSessionID(getValueFromHeaderKey(headers, "ngsesid"));
		p1.setOrigin(getValueFromHeaderKey(headers, "origin"));
		p1.setSerializedObj(headers);
		return p1;
	}

	@RequestMapping(value = "/receive", method = { RequestMethod.GET, RequestMethod.POST }, consumes = {
			"application/json" })
	public void receive(@RequestBody PayloadTO jsonReference, @RequestHeader HttpHeaders headers) {
		receiveFile(setDefaultValuesToWrapper(jsonReference, headers));
	}

	@RequestMapping(value = "/upload", method = { RequestMethod.GET, RequestMethod.POST }, consumes = {
			"application/json" })
	public void upload(@RequestBody PayloadTO jsonReference, @RequestHeader HttpHeaders headers) {
		handleFileUpload(setDefaultValuesToWrapper(jsonReference, headers));
	}

}
