
package com.consultec.esigns.listener.queue;

import static com.consultec.esigns.listener.util.ListenerConstantProperties.KEYSTORE_ACCESS_CONFIGURED;
import static com.consultec.esigns.listener.util.ListenerConstantProperties.LOGGED_USER;
import static com.consultec.esigns.listener.util.ListenerConstantProperties.SIGNATURE_REASON_TEXT;
import static com.consultec.esigns.listener.util.ListenerConstantProperties.URL_TSA;

import java.security.PrivateKey;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jms.IllegalStateRuntimeException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.io.IPostHttpClient;
import com.consultec.esigns.core.queue.IMessageHandler;
import com.consultec.esigns.core.security.SecurityManager;
import com.consultec.esigns.core.transfer.PayloadTO;
import com.consultec.esigns.core.transfer.TransferObjectsUtil;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
import com.consultec.esigns.core.util.MQUtility;
import com.consultec.esigns.core.util.PDFSigner;
import com.consultec.esigns.core.util.PropertiesManager;
import com.consultec.esigns.listener.config.QueueConfig;
import com.consultec.esigns.listener.lang.GeneralErrorListenerException;
import com.consultec.esigns.listener.tasks.LauncherClientApp;
import com.consultec.esigns.listener.util.ListenerErrorTreatmentUtil;
import com.consultec.esigns.listener.util.HttpPostClientImpl;
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

  @SuppressWarnings("unchecked")
  @JmsListener(
      destination = "MQ_SERVER")
  public void processMsg(String msg) {

    PayloadTO pobj = TransferObjectsUtil.readObject(msg);

    if (pobj == null) {

      throw new IllegalStateRuntimeException("Error parsing msg");

    }

    final String sessionId = pobj.getSessionID();
    String path = pobj.getOrigin() + "/o/api/account-opening/receive-signature";
    Stage stage = pobj.getStage();

    FileSystemManager manager = FileSystemManager.getInstance();
    Boolean doIt = Boolean
        .valueOf(PropertiesManager.getInstance().getValue(PropertiesManager.DELETE_DATA_ON_EXIT));


    try {

      switch (stage) {

        case INIT:
          manager.init(sessionId);
          manager.createLocalWorkspace(sessionId, pobj.getPlainDocEncoded());
          manager.serializeObjectFile(pobj);
          ExecutorService executor = Executors.newSingleThreadExecutor();
          Future<Integer> taskLauncher = executor.submit(new LauncherClientApp(sessionId));

          if (taskLauncher.isDone()) {

            logger.info("Launcher client app got value :" + taskLauncher.get());

          }

          break;
        case MANUAL_SIGNED:
          manager.checkConsistency(sessionId);
          SecurityManager helper = SecurityManager.getInstance();
          String alias = helper.getAlias();

          logger.info(" Getting certificates from Keystore under alias : [" + alias + "]");

          PrivateKey signPrivateKey = (PrivateKey) helper.getPrivateKeyByAlias(alias);
          IExternalSignature pks = new PrivateKeySignature(signPrivateKey, DigestAlgorithms.SHA256,
              helper.getProviderName());

          logger.info("Signature process is started");

          PDFSigner signer = new PDFSigner.Builder(KEYSTORE_ACCESS_CONFIGURED.getDigestProvider(),
              pks, helper.getCertificateByAlias(alias), helper.getCertificateChainByAlias(alias),
              manager.getPdfStrokedDoc().getAbsolutePath(),
              manager.getPdfEsignedDoc().getAbsolutePath())
                  .reason(Optional.of(SIGNATURE_REASON_TEXT))
                  .tsaServerURL(Optional.ofNullable(URL_TSA))
                  .location(Optional.of(PropertiesManager.getInstance()
                      .getValue(PropertiesManager.PROPERTY_USER_STROKE_LOCATION)))
                  .userName(Optional.ofNullable(LOGGED_USER)).build();

          if (!signer.sign()) {
            throw new GeneralErrorListenerException(
                "Error trying to send PDF signed to Listener. Invalid signature");
          }

          EventLogger.getInstance()
              .info("Se ha realizado la firma electronica del documento asociado a la session : ["
                  + sessionId + "]");

          if (signer.basicCheckSignedDoc()) {

            PayloadTO p = TransferObjectsUtil.buildPayloadFromDrive();
            ObjectMapper objectMapper = new ObjectMapper();
            String pckg = objectMapper.writeValueAsString(p);
            logger.info(" Enviando paquete de vuelta a Stella ");

            MQUtility.sendMessageMQ(QueueConfig.class, MessageSender.class, pckg);
            manager.deleteOnExit(doIt);
            break;

          }

          throw new GeneralErrorListenerException(
              "Error trying to send PDF signed to Listener. Invalid signature");

        case COMPLETED:
          path = pobj.getOrigin() + "/o/api/account-opening/receive-status";
        case E_SIGNED:

          ObjectMapper objectMapper = new ObjectMapper();
          String pckg = objectMapper.writeValueAsString(pobj);
          HttpEntity stringEntity = new StringEntity(pckg, ContentType.APPLICATION_JSON);

          IPostHttpClient httpPost = new HttpPostClientImpl(path);
          httpPost.setEntity(stringEntity);
          httpPost.setCookie(pobj.getCookieHeader());
          httpPost.fillHeader((LinkedHashMap<String, List<String>>) pobj.getSerializedObj());
          CloseableHttpResponse response = httpPost.execute();

          logger.info("Respuesta del servicio de envío hacia Stella "
              + response.getStatusLine().getStatusCode());

          EventLogger.getInstance().info("Se envia el documento relacionado a la session : ["
              + pobj.getSessionID() + "] al servidor [" + pobj.getOrigin() + "]");
          break;

        default:
          break;
      }

    } catch (Exception e) {

      ListenerErrorTreatmentUtil.treatTheError(pobj, e, false);

    }
  }

}
