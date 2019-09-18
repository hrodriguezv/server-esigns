
package com.consultec.esigns.core.transfer;

import java.io.Serializable;

import lombok.Data;

/**
 * The Class Post.
 *
 * @author hrodriguez
 */
@Data
public class PayloadTO implements Serializable {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -4053174635248731108L;

  /**
   * The Enum Stage.
   */
  public enum Stage {

    /** The init stage. */
    INIT,
    /** The manual signed. */
    MANUAL_SIGNED,
    /** The e signed. */
    E_SIGNED,
    /** The completed. */
    COMPLETED,

    CANCELLED,

    ERROR,
    
    CERTIFICATE_ERROR,
    
    REPOSITORY_ERROR

  }

  /** The stage. */
  private Stage stage;

  /** The plain doc encoded. */
  private String plainDocEncoded;

  /** The stroked doc encoded. */
  private String strokedDocEncoded;

  /** The signed doc encoded. */
  private String signedDocEncoded;

  /** The strokes. */
  private String[] strokes;

  /** The images. */
  private String[] images;

  /** The session ID. */
  private String sessionID;

  /** The origin. */
  private String callbackUrl;

  /** The user logged. */
  private String userLogged;

  /** The serialized obj. */
  private Object serializedObj;

  /** The cookie header. */
  private String cookieHeader;

  /** The code. */
  private String code;

  /**
   * Numero de ente del cliente.
   */
  private int entityId;

  /**
   * Extension del archivo.
   */
  private String fileExtension;

}
