
package com.consultec.esigns.core.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.esf.OtherHashAlgAndValue;
import org.bouncycastle.asn1.esf.SignaturePolicyId;
import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consultec.esigns.core.util.InetUtility;

/**
 * The Class SecurityProvider.
 *
 * @author hrodriguez
 */
@SuppressWarnings("deprecation")
public class SecurityManager {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(SecurityManager.class);

  /** The provider. */
  private Provider provider;

  /** The key store. */
  private KeyStore keyStore;

  /** The mode. */
  private KeyStoreAccessMode mode;

  /** The map keys. */
  private Map<String, Key> mapKeys;

  /** The map trusted chain. */
  private Map<String, Certificate[]> mapTrustedChain;

  /** The map certificate. */
  private Map<String, X509Certificate> mapCertificate;

  /** The instance. */
  private static SecurityManager instance;
  /** The mutex. */
  private static Object mutex = new Object();

  /**
   * Instantiates a new security provider.
   *
   * @param mode the mode
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  private SecurityManager() {}

  /**
   * @param clazz
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  private static Provider getNewInstanceProvider(Class<?> clazz)
      throws InstantiationException, IllegalAccessException {

    return (Provider) clazz.newInstance();
  }

  public static SecurityManager getInstance() {

    SecurityManager result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null)
          instance = result = new SecurityManager();
      }
    }
    return result;
  }

  /**
   * Gets the PAdES EPES profile to set in signature object.
   *
   * @param digest the digest
   * @return the PAdES EPES profile
   * @see #com.itextpdf.signatures.DigestAlgorithms.getAllowedDigest
   *      DigestAlgorithms.getAllowedDigest("SHA1")
   */
  // DigestAlgorithms.getAllowedDigest("SHA1")
  public static SignaturePolicyIdentifier getPadesEpesProfile(String digest) {

    String notExistingSignaturePolicyOid = "2.16.724.631.3.1.124.2.29.9";
    ASN1ObjectIdentifier asn1PolicyOid =
        DERObjectIdentifier.getInstance(new DERObjectIdentifier(notExistingSignaturePolicyOid));
    AlgorithmIdentifier hashAlg = new AlgorithmIdentifier(new ASN1ObjectIdentifier(digest));
    // indicate that the policy hash value is not known; see ETSI TS 101 733
    // V2.2.1, 5.8.1
    byte[] zeroSigPolicyHash = {
        0};
    DEROctetString hash = new DEROctetString(zeroSigPolicyHash);

    SignaturePolicyId signaturePolicyId =
        new SignaturePolicyId(asn1PolicyOid, new OtherHashAlgAndValue(hashAlg, hash));
    return new SignaturePolicyIdentifier(signaturePolicyId);
  }

  /**
   * Gets the alias given the mode which the helper was initialized.
   *
   * @return the alias
   */
  public String getAlias() {

    switch (mode) {
      case FILE_SYSTEM:
        return this.getAliases().next();
      case WINDOWS_MY:
      case WINDOWS_ROOT:
        return InetUtility.getLoggedUserNameExt();
      case LOCAL_MACHINE:
        return getAliasByCommonName(InetUtility.getHostName());
      default:
        break;
    }
    return null;
  }

  private KeyStore getConfiguredKeyStore() throws KeyStoreException, NoSuchProviderException {

    switch (instance.mode) {
      case FILE_SYSTEM:
        return KeyStore.getInstance(mode.getType(), getProviderName());
      case WINDOWS_MY:
      case WINDOWS_ROOT:
      case LOCAL_MACHINE:
        return KeyStore.getInstance(mode.getType());
      default:
        break;
    }
    return null;
  }

  /**
   * Gets the alias by common name.
   *
   * @param string the string
   * @return the alias by common name
   */
  private String getAliasByCommonName(String string) {

    return mapCertificate.entrySet().stream().filter(entry -> {
      String alias = null;
      X500Name x500name;
      try {
        x500name = new JcaX509CertificateHolder(entry.getValue()).getSubject();
        if (x500name.getRDNs(BCStyle.CN).length > 0) {
          RDN cn = x500name.getRDNs(BCStyle.CN)[0];
          alias = IETFUtils.valueToString(cn.getFirst().getValue());
        }
      } catch (CertificateEncodingException e) {
        logger.error("Error getting the alias given the common name [" + string + "]", e);
      }
      return string.equals(alias);
    }).map(Map.Entry::getKey).findFirst().orElse(null);
  }

  /**
   * Initialize all security objects required to sign documents.
   *
   * @param provider the provider
   * @param p12file the p 12 file
   * @param pwd the pwd
   */
  public void init(KeyStoreAccessMode mode, Optional<String> p12file, char[] pwd) {

    try {
      instance.mode = mode;
      instance.provider = getNewInstanceProvider(mode.getProvider());
      Security.addProvider(provider);
    } catch (InstantiationException | IllegalAccessException e1) {
      logger.error("Error trying initialize the configured keystore", e1);
      throw new IllegalStateException("Error trying initialize the configured keystore", e1);
    }

    try {
      keyStore = getConfiguredKeyStore();
    } catch (KeyStoreException | NoSuchProviderException e) {
      logger.error("Error trying initialize the configured keystore", e);
    }

    try {
      if (p12file.isPresent()) {
        keyStore.load(new FileInputStream(p12file.get()), pwd);
      } else {
        keyStore.load(null, pwd);
      }
    } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
      logger.error("Error trying to load the configured keystore", e);
    }

    try {
      mapKeys = new HashMap<>();
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        String element = aliases.nextElement();
        mapKeys.put(element, keyStore.getKey(element, pwd));
      }
    } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
      logger.error("Error getting keys from the configured keystore", e);
    }

    try {
      mapTrustedChain = new HashMap<>();
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        String element = aliases.nextElement();
        mapTrustedChain.put(element, (Certificate[]) keyStore.getCertificateChain(element));
      }
    } catch (KeyStoreException e) {
      logger.error("Error getting trusted chain from the configured keystore", e);
    }

    try {
      mapCertificate = new HashMap<>();
      Enumeration<String> aliases = keyStore.aliases();
      while (aliases.hasMoreElements()) {
        String element = aliases.nextElement();
        mapCertificate.put(element, (X509Certificate) keyStore.getCertificate(element));
      }
    } catch (KeyStoreException e) {
      logger.error("Error getting certificates from the configured keystore", e);

    }
  }

  /**
   * Gets the key store.
   *
   * @return the key store
   */
  public KeyStore getKeyStore() {

    return keyStore;
  }

  /**
   * Gets the provider name.
   *
   * @return the provider name
   */
  public String getProviderName() {

    return this.provider.getName();
  }

  /**
   * Gets the map keys.
   *
   * @return the map keys
   */
  public Map<String, Key> getMapKeys() {

    return mapKeys;
  }

  /**
   * Gets the private key by alias.
   *
   * @param alias the alias
   * @return the private key by alias
   */
  public Key getPrivateKeyByAlias(String alias) {

    return mapKeys.get(alias);
  }

  /**
   * Gets the aliases.
   *
   * @return the aliases
   */
  public Iterator<String> getAliases() {

    return mapKeys.keySet().iterator();
  }

  /**
   * Gets the certificate chain by alias.
   *
   * @param alias the alias
   * @return the certificate chain by alias
   */
  public Certificate[] getCertificateChainByAlias(String alias) {

    return mapTrustedChain.get(alias);
  }

  /**
   * Gets the map trusted chain.
   *
   * @return the map trusted chain
   */
  public Map<String, Certificate[]> getMapTrustedChain() {

    return mapTrustedChain;
  }

  /**
   * Gets the map certificate.
   *
   * @return the map certificate
   */
  public Map<String, X509Certificate> getMapCertificate() {

    return mapCertificate;
  }

  /**
   * Gets the certificate by alias.
   *
   * @param alias the alias
   * @return the certificate by alias
   */
  public Certificate getCertificateByAlias(String alias) {

    return mapCertificate.get(alias);
  }
}
