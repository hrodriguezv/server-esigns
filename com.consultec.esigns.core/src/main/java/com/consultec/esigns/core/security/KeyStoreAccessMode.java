
package com.consultec.esigns.core.security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.pheox.jcapi.JCAPIProvider;

/**
 * The Enum KeyStoreAccessMode.
 *
 * @author hrodriguez
 */
@SuppressWarnings("restriction")
public enum KeyStoreAccessMode {

  /** The local machine. */
  LOCAL_MACHINE(JCAPIProvider.class, new BouncyCastleDigest(), "msks"),

  /** The windows my. */
  // WINDOWS_MY(SunMSCAPI.class, new BouncyCastleDigest(), "Windows-MY"),
  WINDOWS_MY(BouncyCastleProvider.class, new BouncyCastleDigest(), "Windows-MY"),

  /** The windows root. */
  // WINDOWS_ROOT(SunMSCAPI.class, new BouncyCastleDigest(), "Windows-ROOT"),
  WINDOWS_ROOT(BouncyCastleProvider.class, new BouncyCastleDigest(), "Windows-ROOT"),

  /** The file system. */
  FILE_SYSTEM(BouncyCastleProvider.class, new BouncyCastleDigest(), "pkcs12"),

  /** None */
  NONE(BouncyCastleProvider.class, new BouncyCastleDigest(), "none");

  /** The provider. */
  private Class<?> provider;

  /** The digest provider. */
  private IExternalDigest digestProvider;

  /** The type. */
  private String type;

  /**
   * Instantiates a new key store access mode.
   *
   * @param p the p
   * @param ix the ix
   * @param t the t
   */
  private KeyStoreAccessMode(Class<?> p, IExternalDigest ix, String t) {

    this.provider = p;
    this.digestProvider = ix;
    this.type = t;
  }

  /**
   * Gets the provider.
   *
   * @return the provider
   */
  public Class<?> getProvider() {

    return provider;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType() {

    return type;
  }

  /**
   * Gets the digest provider.
   *
   * @return the digest provider
   */
  public IExternalDigest getDigestProvider() {

    return digestProvider;
  }

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
