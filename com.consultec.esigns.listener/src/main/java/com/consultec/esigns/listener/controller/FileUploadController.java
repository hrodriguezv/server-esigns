
package com.consultec.esigns.listener.controller;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.listener.config.QueueConfig;
import com.consultec.esigns.listener.queue.MessageSender;
import com.consultec.esigns.listener.storage.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class FileUploadController {

	StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService) {

		this.storageService = storageService;
	}

	@PostMapping("/files/upload")
	public String handleFileUpload(
		@RequestParam("file") MultipartFile file,
		@RequestParam("code") String code, 
		@RequestHeader HttpHeaders headers,
		RedirectAttributes redirectAttributes) {

		String message;
		try {
			String sessionId = "Consultec-ID";
			String contentFile =
				Base64.getEncoder().encodeToString(file.getBytes());

			PayloadTO pobj = new PayloadTO();
			pobj.setStage(Stage.MANUAL_SIGNED);
			pobj.setSessionID(sessionId);
			pobj.setStrokedDocEncoded(contentFile);

			FileSystemManager manager = FileSystemManager.getInstance();

			manager.init(sessionId);
			manager.createLocalWorkspace(
				pobj.getStage(), sessionId, pobj.getStrokedDocEncoded());

			manager.serializeObjectFile(pobj);
			// storageService.store(file);

			ObjectMapper objectMapper = new ObjectMapper();
			String pckg = objectMapper.writeValueAsString(pobj);

			MQUtility.sendMessageMQ(
				QueueConfig.class, MessageSender.class, pckg);

			message =
				"You successfully uploaded " + file.getOriginalFilename() + "!";
		}
		catch (Exception e) {
			message = "You cannot upload file (" + file.getOriginalFilename() +
				") due to :" + e.getMessage();
		}
		redirectAttributes.addFlashAttribute("message", message);

		return "redirect:/";
	}

}
