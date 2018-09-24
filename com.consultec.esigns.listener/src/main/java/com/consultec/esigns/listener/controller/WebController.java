package com.consultec.esigns.listener.controller;

import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.consultec.esigns.core.io.IOUtility;
import com.consultec.esigns.core.model.PayloadTO;
import com.consultec.esigns.core.model.PayloadTO.Stage;
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

	@RequestMapping(value="/receive", method = { RequestMethod.GET, RequestMethod.POST }, consumes = { "application/json" })
    public String receive(@RequestBody PayloadTO p1) {
		doWork(p1);
		return "receiving payload {" + p1.getPlainDocEncoded() + "}";
    }
	/**
	 * Do work.
	 *
	 * @param base
	 *            the base
	 */
	private static void doWork(PayloadTO pobj) {
		try {
			// get an unique identifier to create local workspace on File system
			MessageDigest salt = MessageDigest.getInstance("SHA-256");
			salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
			String digest = IOUtility.bytesToHex(salt.digest());
			System.out.println("uuid = " + digest);
			// set initial values to payload object
			pobj.setSessionID(digest);
			pobj.setStage(Stage.INIT);
			// serialize and send package to queue in form of a json object
			ObjectMapper objectMapper = new ObjectMapper();
			String pckg = objectMapper.writeValueAsString(pobj);
			MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);
		} catch (Exception e) {
			// TODO set logger
			e.printStackTrace();
		} finally {
			//TODO close anything closeable here
		}
	}

}
