
package com.consultec.esigns.listener.queue;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.queue.IMessageHandler;
import com.consultec.esigns.core.security.KeyStoreAccessMode;
import com.consultec.esigns.core.security.SecurityHelper;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.util.InetUtility;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.core.util.PDFSigner;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.config.QueueConfig;
import com.consultec.esigns.listener.util.ListenerConstantProperties;
import com.consultec.esigns.listener.util.TransferObjectsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PrivateKeySignature;
import com.pheox.jcapi.JCAPIProperties;
import com.pheox.jcapi.JCAPISystemStoreRegistryLocation;

/**
 * The Class MessageHandler.
 */
@Component
public class MessageHandler implements IMessageHandler {

	/** The Constant logger. */
	private static final Logger logger =
		LoggerFactory.getLogger(MessageHandler.class);

	/*
	 * (non-Javadoc)
	 * @see
	 * com.consultec.esigns.core.queue.IMessageHandler#processMsg(java.lang.
	 * String)
	 */
	@JmsListener(destination = "MQ_SERVER")
	public void processMsg(String msg) {

		FileSystemManager manager = FileSystemManager.getInstance();
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			final PayloadTO pobj = objectMapper.readValue(msg, PayloadTO.class);
			switch (pobj.getStage()) {
			case INIT:
				manager.init(pobj.getSessionID());
				manager.createLocalWorkspace(
					pobj.getSessionID(), pobj.getPlainDocEncoded());
				manager.serializeObjectFile(pobj);

				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.submit(() -> {
					try {
						ListenerConstantProperties.launchClientApp(
							pobj.getSessionID());
						EventLogger.getInstance().info(
							"Se inicia el proceso de firma digital. Session ID: [" +
								pobj.getSessionID() + "]");
					}
					catch (IOException e) {
						logger.error(
							"There was an error trying start the launcher of PDF viewer : [" +
								e.getMessage() + "]",
							e);
						EventLogger.getInstance().error(
							"Hubo un error ejecutando el launcher del visualizador de PDF's : [" +
								e.getMessage() + "]");
					}
				});
				break;
			case MANUAL_SIGNED:
				char[] pwd = null;
				Optional<String> nill = Optional.ofNullable(null);
				KeyStoreAccessMode keystoreAccess =
					KeyStoreAccessMode.fromString(
						ListenerConstantProperties.KEYSTORE_ACCESS_MODE);
				try {
					manager.checkConsistency(pobj.getSessionID());
					if (!InetUtility.isReachable(
						ListenerConstantProperties.URL_TSA)) {
						logger.error(
							"There was a timeout error because TSA Server is not reachable [" +
								ListenerConstantProperties.URL_TSA + "]");
					}
					switch (keystoreAccess) {
					case LOCAL_MACHINE:
						JCAPISystemStoreRegistryLocation location =
							new JCAPISystemStoreRegistryLocation(
								JCAPISystemStoreRegistryLocation.CERT_SYSTEM_STORE_LOCAL_MACHINE);
						JCAPIProperties.getInstance().setSystemStoreRegistryLocation(
							location);

					case WINDOWS_MY:
					case WINDOWS_ROOT:
						break;
					default:
						nill = Optional.of(
							manager.getCertificate().getAbsolutePath());
						pwd = PropertiesManager.getInstance().getValue(
							PropertiesManager.KEY_OPERATOR_CERTIFICATE).toCharArray();
					}

					SecurityHelper helper = new SecurityHelper(keystoreAccess);
					helper.init(nill, nill, pwd);
					String alias = helper.getAlias();
					logger.info(
						" Getting certificates from Keystore under alias : [" +
							alias + "]");

					Certificate[] signChain =
						helper.getCertificateChainByAlias(alias);
					PrivateKey signPrivateKey =
						(PrivateKey) helper.getPrivateKeyByAlias(alias);
					IExternalSignature pks = new PrivateKeySignature(
						signPrivateKey, DigestAlgorithms.SHA256,
						keystoreAccess.getProvider().getName());
					Certificate certificate =
						helper.getCertificateByAlias(alias);

					logger.info("Signature process is started");

					PDFSigner signer = new PDFSigner.Builder(
						keystoreAccess.getDigestProvider(), pks, certificate,
						signChain, manager.getPdfStrokedDoc().getAbsolutePath(),
						manager.getPdfEsignedDoc().getAbsolutePath(),
						ListenerConstantProperties.URL_TSA).reason(
							Optional.of(
								ListenerConstantProperties.SIGNATURE_REASON_TEXT)).location(
									Optional.of(
										PropertiesManager.getInstance().getValue(
											PropertiesManager.PROPERTY_USER_STROKE_LOCATION))).userName(
												Optional.of(
													ListenerConstantProperties.LOGGED_USER)).build();
					signer.sign();
					EventLogger.getInstance().info(
						"Se ha realizado la firma electronica del documento asociado a la session : [" +
							pobj.getSessionID() + "]");

					if (signer.basicCheckSignedDoc()) {
						PayloadTO p = TransferObjectsUtil.buildPayloadFromDrive();
						objectMapper = new ObjectMapper();
						String pckg = objectMapper.writeValueAsString(p);
						logger.info(" Enviando paquete de vuelta a Stella ");

						MQUtility.sendMessageMQ(
							QueueConfig.class, MessageSender.class, pckg);
						Boolean doIt = Boolean.valueOf(
							PropertiesManager.getInstance().getValue(
								PropertiesManager.DELETE_DATA_ON_EXIT));
						manager.deleteOnExit(doIt);
					}
					else {
						throw new RuntimeException(
							"Error trying to send PDF signed to Listener. Invalid signature");
					}
				}
				catch (Throwable e) {
					String msgError =
						"General error trying to sign the PdfDocument with sessionid : [" +
							pobj.getSessionID() + "]  due to [" +
							e.getMessage() + "]";
					logger.error(msgError, e);
					EventLogger.getInstance().error(msgError);
				}
				break;
			case E_SIGNED:
				CloseableHttpClient httpclient = HttpClients.createDefault();
				HttpPost httpPost = new HttpPost(
					pobj.getOrigin() +
						"/o/api/account-opening/receive-signature");
				objectMapper = new ObjectMapper();
				String pckg = objectMapper.writeValueAsString(pobj);
				HttpEntity stringEntity =
					new StringEntity(pckg, ContentType.APPLICATION_JSON);
				httpPost.setEntity(stringEntity);
				httpPost.addHeader("Cookie", pobj.getCookieHeader());

				@SuppressWarnings("unchecked")
				LinkedHashMap<String, List<String>> header =
					(LinkedHashMap<String, List<String>>) pobj.getSerializedObj();
				header.keySet().stream().forEach(
					k -> httpPost.addHeader(k, header.get(k).get(0)));
				httpPost.removeHeader(httpPost.getAllHeaders()[3]); // remueve
																	// el length

				@SuppressWarnings("unused")
				CloseableHttpResponse response2 = httpclient.execute(httpPost);
				EventLogger.getInstance().info(
					"Se envia el documento relacionado a la session : [" +
						pobj.getSessionID() + "] al servidor [" +
						pobj.getOrigin() + "]");
				break;
			default:
				break;
			}
		}
		catch (IOException e) {
			String msgError =
				"Error processing message : [" + e.getMessage() + "]";
			logger.error(msgError, e);
			EventLogger.getInstance().error(msgError);
		}
	}
}
