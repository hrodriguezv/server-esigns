
package com.consultec.esigns.listener.queue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.io.IOUtility;
import com.consultec.esigns.core.model.PayloadTO;
import com.consultec.esigns.core.model.PayloadTO.Stage;
import com.consultec.esigns.core.queue.IMessageHandler;
import com.consultec.esigns.core.security.KeyStoreAccessMode;
import com.consultec.esigns.core.security.SecurityHelper;
import com.consultec.esigns.core.util.InetUtility;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.core.util.PDFSignatureUtil;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.config.QueueConfig;
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

	/** The Constant PDFVIEWER_EXECPATH. */
	private static final String PDFVIEWER_EXECPATH =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_ICEPDF_PATH);

	/** The Constant PDFVIEWER_EXECDEP. */
	private static final String PDFVIEWER_EXECDEP =
		PropertiesManager.getInstance().getValue(
			PropertiesManager.PROPERTY_ICEPDF_DLLS);

	/**
	 * Command simplified line.
	 *
	 * @param id
	 *            the id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void commandSimplifiedLine(String id)
		throws IOException {

		String[] cmd = {
			"java", "-Djava.library.path=\"" + PDFVIEWER_EXECDEP + "\";",
			"-jar", "icepdf-viewer-6.3.1-SNAPSHOT.jar", "-sessionid", id
		};
		BufferedReader input = null;
		try {
			Process p = Runtime.getRuntime().exec(
				cmd, null, new File(PDFVIEWER_EXECPATH));
			input =
				new BufferedReader(new InputStreamReader(p.getErrorStream()));
		}
		catch (Exception e) {
			logger.error(
				"There was an error trying to launch icepdf viewer ", e);
			e.printStackTrace();
		}
		finally {
			input.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.consultec.esigns.core.queue.IMessageHandler#processMsg(java.lang.
	 * String)
	 */
	@JmsListener(destination = "MQ_SERVER")
	public void processMsg(String msg) {

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			final PayloadTO pobj = objectMapper.readValue(msg, PayloadTO.class);
			switch (pobj.getStage()) {
			case INIT:
				// create local workspace
				// write the received base64 package
				String path = PropertiesManager.getInstance().getValue(
					PropertiesManager.PROPERTY_USER_BASE_HOME);
				String docName = PropertiesManager.getInstance().getValue(
					PropertiesManager.PROPERTY_USER_HOME_PDFDOCUMENT);
				String file = path + "/" + pobj.getSessionID() + "/" + docName;
				IOUtility.writeDecodedContent(file, pobj.getPlainDocEncoded());

				FileSystemManager.getInstance().init(pobj.getSessionID());
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.submit(() -> {
					try {
						commandSimplifiedLine(pobj.getSessionID());
					}
					catch (IOException e) {
						logger.error(
							"Unable to launch the PDF viewer due to: [" +
								e.getMessage() + "]",
							e);
					}
				});
				break;
			case MANUAL_SIGNED:
				FileSystemManager manager = FileSystemManager.getInstance();
				String urlTSA = PropertiesManager.getInstance().getValue(
					PropertiesManager.TSA_URL_SERVER);
				char[] pwd = null;
				String loggedUsr = InetUtility.getLoggedUserNameExt();
				MessageFormat formatter = new MessageFormat(
					PropertiesManager.getInstance().getValue(
						PropertiesManager.PROPERTY_USER_STROKE_REASON));
				String reason = formatter.format(
					new Object[] {
						loggedUsr
					});
				String keystoreAccessMode =
					PropertiesManager.getInstance().getValue(
						PropertiesManager.KEYSTORE_ACCESS_MODE);
				Optional<String> nill = Optional.ofNullable(null);
				KeyStoreAccessMode keystoreAccess =
					KeyStoreAccessMode.fromString(keystoreAccessMode);
				try {
					manager.checkConsistency(pobj.getSessionID());
					if (!InetUtility.isReachable(urlTSA)) {
						logger.error(
							"There was a timeout error because TSA Server is not reachable [" +
								urlTSA + "]");
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
							PropertiesManager.PASSWORD_OPERATOR_CERTIFICATE).toCharArray();
					}

					SecurityHelper helper = new SecurityHelper(keystoreAccess);
					helper.init(nill, nill, pwd);
					String alias = helper.getAlias();
					logger.info(
						" Obteniendo certificados requeridos para firmar ");

					Certificate[] signChain =
						helper.getCertificateChainByAlias(alias);
					PrivateKey signPrivateKey =
						(PrivateKey) helper.getPrivateKeyByAlias(alias);
					IExternalSignature pks = new PrivateKeySignature(
						signPrivateKey, DigestAlgorithms.SHA256,
						keystoreAccess.getProvider().getName());;
					Certificate certificate =
						helper.getCertificateByAlias(alias);

					logger.info(" Realizando firma del documento ");

					PDFSignatureUtil.signAddingPadesEpesProfile(
						keystoreAccess.getDigestProvider(), certificate, pks,
						signChain, manager.getPdfStrokedDoc().getAbsolutePath(),
						manager.getPdfEsignedDoc().getAbsolutePath(), urlTSA,
						Optional.of(reason),
						Optional.of(
							PropertiesManager.PROPERTY_USER_STROKE_LOCATION),
						Optional.of(loggedUsr));

					if (PDFSignatureUtil.basicCheckSignedDoc(
						manager.getPdfEsignedDoc().getAbsolutePath(),
						"Signature1")) {
						PayloadTO p = buildPayload();
						objectMapper = new ObjectMapper();
						String pckg = objectMapper.writeValueAsString(p);
						logger.info(" Enviando paquete de vuelta a Stella ");

						MQUtility.sendMessageMQ(
							QueueConfig.class, MessageSender.class, pckg);
						Boolean doIt = Boolean.valueOf(
							PropertiesManager.getInstance().getValue(
								PropertiesManager.DELETE_DATA_ON_EXIT));
						FileSystemManager.getInstance().deleteOnExit(doIt);
					}
					else {
						throw new RuntimeException(
							"Error trying to send PDF signed to Listener. Invalid signature");
					}
				}
				catch (Throwable e) {
					logger.error(
						"General error trying to sign the PdfDocument with sessionid : [" +
							pobj.getSessionID() + "]  due to [" +
							e.getMessage() + "]",
						e);
				}
				break;
			case E_SIGNED:
				// TODO send pckg to Stella.
				break;
			default:
				break;
			}
		}
		catch (IOException e) {
			logger.error(
				"Error processing message : [" + e.getMessage() + "]", e);
		}
	}

	/**
	 * Builds the payload.
	 *
	 * @return the payload TO
	 */
	private PayloadTO buildPayload() {

		PayloadTO post = new PayloadTO();
		post.setSessionID(FileSystemManager.getInstance().getSessionId());
		post.setStage(Stage.E_SIGNED);

		byte[] strokedFile = null;
		try {
			strokedFile = FileUtils.readFileToByteArray(
				FileSystemManager.getInstance().getPdfStrokedDoc());
		}
		catch (IOException e) {
			logger.error(
				"Error building JSON Object: [" + e.getMessage() + "]", e);
		}
		post.setStrokedDocEncoded(
			Base64.getEncoder().encodeToString(strokedFile));

		List<String> strokeList = new ArrayList<>();
		for (File b : FileSystemManager.getInstance().getTextStrokeFiles()) {
			try {
				strokeList.add(new String(FileUtils.readFileToString(b)));
			}
			catch (IOException e) {
				logger.error(
					"Error building JSON Object: [" + e.getMessage() + "]", e);
			}
		}
		post.setStrokes(strokeList.toArray(new String[0]));

		List<String> imgList = new ArrayList<>();
		for (File b : FileSystemManager.getInstance().getImageStrokeFiles()) {
			try {
				imgList.add(new String(FileUtils.readFileToString(b)));
			}
			catch (IOException e) {
				logger.error(
					"Error building JSON Object: [" + e.getMessage() + "]", e);
			}
		}
		post.setImages(imgList.toArray(new String[0]));

		byte[] eSignedFile = null;
		try {
			eSignedFile = FileUtils.readFileToByteArray(
				FileSystemManager.getInstance().getPdfEsignedDoc());
		}
		catch (IOException e) {
			logger.error(
				"Error building JSON Object: [" + e.getMessage() + "]", e);
		}
		post.setSignedDocEncoded(
			Base64.getEncoder().encodeToString(eSignedFile));
		return post;
	}

}