
package com.consultec.esigns.core.transfer;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consultec.esigns.core.events.EventLogger;
import com.consultec.esigns.core.io.FileSystemManager;
import com.consultec.esigns.core.transfer.PayloadTO.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class TransferObjectsUtil.
 *
 * @author hrodriguez
 */
public class TransferObjectsUtil {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(TransferObjectsUtil.class);

  /**
   * Instantiates a new transfer objects util.
   */
  private TransferObjectsUtil() {

  }

  /**
   * Builds the payload from drive.
   *
   * @return the payload TO
   */
  public static PayloadTO buildPayloadFromDrive() {

    String errorMsg = "Error building JSON Object: [{0}]";
    MessageFormat formatter = new MessageFormat(errorMsg);

    PayloadTO post;
    try {

      post = (PayloadTO) FileSystemManager.getInstance().deserializeObject();

    } catch (IOException e1) {

      logger.error(formatter.format(e1.getMessage()), e1);
      post = new PayloadTO();

    }

    post.setSessionID(FileSystemManager.getInstance().getSessionId());
    post.setStage(Stage.E_SIGNED);

    if (FileSystemManager.getInstance().getPdfStrokedDoc().exists()) {

      byte[] strokedFile = null;
      try {

        strokedFile =
            FileUtils.readFileToByteArray(FileSystemManager.getInstance().getPdfStrokedDoc());
        post.setStrokedDocEncoded(Base64.getEncoder().encodeToString(strokedFile));

      } catch (IOException e) {

        logger.error(formatter.format(e.getMessage()), e);

      }

    }

    List<String> strokeList = new ArrayList<>();
    for (File b : FileSystemManager.getInstance().getTextStrokeFiles()) {

      try {

        strokeList.add(new String(FileUtils.readFileToString(b)));

      } catch (IOException e) {

        logger.error(formatter.format(e.getMessage()), e);

      }

    }
    post.setStrokes(strokeList.toArray(new String[0]));

    List<String> imgList = new ArrayList<>();
    for (File b : FileSystemManager.getInstance().getImageStrokeFiles()) {

      try {

        imgList.add(Base64.getEncoder().encodeToString(FileUtils.readFileToByteArray(b)));

      } catch (IOException e) {

        logger.error(formatter.format(e.getMessage()), e);

      }

    }

    post.setImages(imgList.toArray(new String[0]));

    if (FileSystemManager.getInstance().getPdfEsignedDoc().exists()) {

      byte[] eSignedFile = null;
      try {

        eSignedFile =
            FileUtils.readFileToByteArray(FileSystemManager.getInstance().getPdfEsignedDoc());
        post.setSignedDocEncoded(Base64.getEncoder().encodeToString(eSignedFile));

      } catch (IOException e) {

        logger.error(formatter.format(e.getMessage()), e);

      }

    }

    return post;
  }

  /**
   * Read object.
   *
   * @param msg the msg
   * @return the payload TO
   */
  public static PayloadTO readObject(String msg) {

    ObjectMapper objectMapper = new ObjectMapper();

    try {
      return objectMapper.readValue(msg, PayloadTO.class);
    } catch (IOException e) {
      String msgError = "Error processing message - serializing reading : [" + e.getMessage() + "]";
      logger.error(msgError, e);
      EventLogger.getInstance().error(msgError);
    }
    return null;
  }

}
