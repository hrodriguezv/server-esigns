
package com.consultec.esigns.core.security;

import java.security.Provider;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.pheox.jcapi.JCAPIProvider;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sun.security.mscapi.SunMSCAPI;

/**
 * The Enum KeyStoreAccessMode.
 *
 * @author hrodriguez
 */
@Getter
@AllArgsConstructor
public enum KeyStoreAccessMode {

  /** The local machine. */
  LOCAL_MACHINE(JCAPIProvider.class, new BouncyCastleDigest(), "msks"),

  /** The windows my. */
  WINDOWS_MY(SunMSCAPI.class, new BouncyCastleDigest(), "Windows-MY"),

  /** The windows root. */
  WINDOWS_ROOT(SunMSCAPI.class, new BouncyCastleDigest(), "Windows-ROOT"),

  /** The file system. */
  FILE_SYSTEM(BouncyCastleProvider.class, new BouncyCastleDigest(), "pkcs12"),

  /** None */
  NONE(BouncyCastleProvider.class, new BouncyCastleDigest(), "none");

  /** The provider. */
  private Class<? extends Provider> provider;

  /** The digest provider. */
  private IExternalDigest digestProvider;

  /** The type. */
  private String type;

  /**
   * From string.
   *
   * @param text the text
   * @return the key store access mode
   */
  public static KeyStoreAccessMode fromString(String text) {

    for (KeyStoreAccessMode b : KeyStoreAccessMode.values()) {

      if (b.type.equalsIgnoreCase(text)) {
        return b;
      }

    }

    return null;

  }

}
