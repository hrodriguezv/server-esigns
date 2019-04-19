
package com.consultec.esigns.listener.queue;

import static com.consultec.esigns.listener.util.ListenerConstantProperties.LOGGED_USER;
import static com.consultec.esigns.listener.util.ListenerConstantProperties.SIGNATURE_REASON_TEXT;
import static com.consultec.esigns.listener.util.ListenerConstantProperties.URL_TSA;
import static com.consultec.esigns.listener.util.ListenerConstantProperties.KEYSTORE_ACCESS_CONFIGURED;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.IllegalStateRuntimeException;

import org.apache.http.HttpEntity;
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
import com.consultec.esigns.core.security.SecurityManager;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.core.util.PDFSigner;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.config.QueueConfig;
import com.consultec.esigns.listener.lang.GeneralErrorListenerException;
import com.consultec.esigns.listener.tasks.LauncherClientApp;
import com.consultec.esigns.listener.util.TransferObjectsUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PrivateKeySignature;

/**
 * The Class MessageHandler.
 */
@Component
public class MessageHandler implements IMessageHandler {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.core.queue.IMessageHandler#processMsg(java.lang. String)
   */
  @JmsListener(
      destination = "MQ_SERVER")
  public void processMsg(String msg) {

    PayloadTO pobj = TransferObjectsUtil.readObject(msg);
    if (pobj == null) {
      throw new IllegalStateRuntimeException("Error parsing msg");
    }

    final String sessionId = pobj.getSessionID();
    FileSystemManager manager = FileSystemManager.getInstance();

    switch (pobj.getStage()) {
      case INIT:
        try {
          manager.init(pobj.getSessionID());
          manager.createLocalWorkspace(sessionId, pobj.getPlainDocEncoded());
          manager.serializeObjectFile(pobj);
          ExecutorService executor = Executors.newSingleThreadExecutor();
          Future<Integer> taskLauncher = executor.submit(new LauncherClientApp(sessionId));
          if (taskLauncher.isDone())
            logger.info("Launcher client app got value :" + taskLauncher.get());
        } catch (IOException | InterruptedException | ExecutionException e1) {
          logger.error(
              "There was an error trying to create the workspace : [" + e1.getMessage() + "]", e1);
          EventLogger.getInstance()
              .error("Hubo un error intentando crear el workspace: [" + e1.getMessage() + "]");
          Thread.currentThread().interrupt();
        }
        break;
      case MANUAL_SIGNED:
        try {
          manager.checkConsistency(sessionId);
          SecurityManager helper = SecurityManager.getInstance();
          String alias = helper.getAlias();

          logger.info(" Getting certificates from Keystore under alias : [" + alias + "]");

          PrivateKey signPrivateKey = (PrivateKey) helper.getPrivateKeyByAlias(alias);
          IExternalSignature pks = new PrivateKeySignature(signPrivateKey, DigestAlgorithms.SHA256,
              helper.getProviderName());

          logger.info("Signature process is started");

          PDFSigner signer = new PDFSigner.Builder(KEYSTORE_ACCESS_CONFIGURED.getDigestProvider(), pks,
              helper.getCertificateByAlias(alias), helper.getCertificateChainByAlias(alias),
              manager.getPdfStrokedDoc().getAbsolutePath(),
              manager.getPdfEsignedDoc().getAbsolutePath(), URL_TSA)
                  .reason(Optional.of(SIGNATURE_REASON_TEXT))
                  .location(Optional.of(PropertiesManager.getInstance()
                      .getValue(PropertiesManager.PROPERTY_USER_STROKE_LOCATION)))
                  .userName(Optional.ofNullable(LOGGED_USER)).build();

          signer.sign();

          // EventLogger.getInstance().info(
          // "Se ha realizado la firma electronica del documento asociado a la session : [" +
          // pobj.getSessionID() + "]");

          if (signer.basicCheckSignedDoc()) {
            PayloadTO p = TransferObjectsUtil.buildPayloadFromDrive();
            ObjectMapper objectMapper = new ObjectMapper();
            String pckg = objectMapper.writeValueAsString(p);
            logger.info(" Enviando paquete de vuelta a Stella ");

            MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);
            Boolean doIt = Boolean.valueOf(
                PropertiesManager.getInstance().getValue(PropertiesManager.DELETE_DATA_ON_EXIT));
            manager.deleteOnExit(doIt);
            break;
          }
          throw new GeneralErrorListenerException(
              "Error trying to send PDF signed to Listener. Invalid signature");
        } catch (Exception e) {
          String msgError = "General error trying to sign the PdfDocument with sessionid : ["
              + pobj.getSessionID() + "] - [" + e.getMessage() + "]";
          logger.error(msgError, e);
          EventLogger.getInstance().error(msgError);
        }
        break;
      case E_SIGNED:
        try {
          CloseableHttpClient httpclient = HttpClients.createDefault();
          HttpPost httpPost =
              new HttpPost(pobj.getOrigin() + "/o/api/account-opening/receive-signature");
          ObjectMapper objectMapper = new ObjectMapper();
          String pckg = objectMapper.writeValueAsString(pobj);
          HttpEntity stringEntity = new StringEntity(pckg, ContentType.APPLICATION_JSON);
          httpPost.setEntity(stringEntity);
          httpPost.addHeader("Cookie", pobj.getCookieHeader());

          @SuppressWarnings("unchecked")
          LinkedHashMap<String, List<String>> header =
              (LinkedHashMap<String, List<String>>) pobj.getSerializedObj();
          header.keySet().stream().forEach(k -> httpPost.addHeader(k, header.get(k).get(0)));
          httpPost.removeHeader(httpPost.getAllHeaders()[3]); // remueve
                                                              // el length
          httpclient.execute(httpPost);
          EventLogger.getInstance().info("Se envia el documento relacionado a la session : ["
              + pobj.getSessionID() + "] al servidor [" + pobj.getOrigin() + "]");
        } catch (IOException e) {
          String msgError = "General error trying to send package back to Stella : ["
              + pobj.getSessionID() + "] - [" + e.getMessage() + "]";
          logger.error(msgError, e);
          EventLogger.getInstance().error(msgError);
        }
        break;
      default:
        break;
    }
  }
}
