
package com.consultec.esigns.core.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Predicate;

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
  private Map<String, Key> keys;

  /** The map trusted chain. */
  private Map<String, Certificate[]> trustedChains;

  /** The map certificate. */
  private Map<String, X509Certificate> certificates;

  /** The instance. */
  private static SecurityManager instance;
  /** The mutex. */
  private static Object mutex = new Object();

  /**
   * Instantiates a new security provider.
   */
  private SecurityManager() {}

  /**
   * Gets the new instance provider.
   *
   * @param clazz the clazz
   * @return the new instance provider
   * @throws InstantiationException the instantiation exception
   * @throws IllegalAccessException the illegal access exception
   */
  private static Provider getNewInstanceProvider(Class<?> clazz)
      throws InstantiationException, IllegalAccessException {

    return (Provider) clazz.newInstance();
  }

  /**
   * Gets the single instance of SecurityManager.
   *
   * @return single instance of SecurityManager
   */
  public static SecurityManager getInstance() {

    SecurityManager result = instance;

    if (result == null) {

      synchronized (mutex) {

        result = instance;

        if (result == null) {

          instance = result = new SecurityManager();

        }

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
    byte[] zeroSigPolicyHash = {0};
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
  public String getConfiguredAlias() {

    switch (mode) {
      case FILE_SYSTEM:
        Iterator<String> aliases = getAliases();
        return aliases.hasNext() ? aliases.next() : null;
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

  /**
   * Gets the configured key store.
   *
   * @return the configured key store
   * @throws KeyStoreException the key store exception
   * @throws NoSuchProviderException the no such provider exception
   */
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
   * @param commonName the string
   * @return the alias by common name
   */
  private String getAliasByCommonName(String commonName) {

    Predicate<Entry<String, X509Certificate>> filterByCommonName =
        new Predicate<Entry<String, X509Certificate>>() {

          @Override
          public boolean test(Entry<String, X509Certificate> p) {
            String alias = null;

            try {

              X500Name x500name = new JcaX509CertificateHolder(p.getValue()).getSubject();

              if (x500name.getRDNs(BCStyle.CN).length > 0) {
                RDN cn = x500name.getRDNs(BCStyle.CN)[0];
                alias = IETFUtils.valueToString(cn.getFirst().getValue());
              }

            } catch (CertificateEncodingException e) {

              logger.error("Error getting the alias given the common name [" + commonName + "]", e);

            }

            return commonName.equals(alias);
          }

        };

    return (certificates != null)
        ? certificates.entrySet().stream().filter(filterByCommonName).map(Map.Entry::getKey)
            .findFirst().orElse(null)
        : null;
  }

  /**
   * Initialize all security objects required to sign documents.
   * 
   * @param mode the mode
   * @param p12file the p 12 file
   * @param pwd the pwd
   * @param safe the safe
   */
  private void init(KeyStoreAccessMode mode, Optional<String> p12file, char[] pwd, boolean safe) {

    try {

      setUp(mode, p12file, pwd);
      return;

    } catch (java.lang.Error e) {

      // A runtime error could be thrown in case of JCAPI can't be loaded
      logger.error("[FATAL] Error caused by :", e);

    }

    if (safe) {

      while (getConfiguredAlias() == null
          && (instance.mode.ordinal() < KeyStoreAccessMode.values().length - 1)) {
        setUp(KeyStoreAccessMode.values()[instance.mode.ordinal() + 1], p12file, pwd);
      }

      if (getConfiguredAlias() == null) {

        throw new IllegalStateException(
            "Can't find the correct settings to load security configuration");
      }

    }

  }

  /**
   * Allows to get the default aliases for the configured keystore.
   *
   * @return Enumeration that contains all alias found in the configured keystore
   * @throws KeyStoreException
   */
  private Enumeration<String> getKeystoreAliases() throws KeyStoreException {
    return (keyStore != null) ? keyStore.aliases() : Collections.emptyEnumeration();
  }

  /**
   * Loads into memory the keystore configured.
   *
   * @param pathFile path to find the keystore in file system
   * @param pwd password to open the configured keystore
   * @throws NoSuchAlgorithmException
   * @throws CertificateException
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void loadKeystore(Optional<String> pathFile, char[] pwd)
      throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {

    switch (instance.mode) {

      case FILE_SYSTEM:
        if (pathFile.isPresent()) {

          keyStore.load(new FileInputStream(pathFile.get()), pwd);

        }
        break;
      case WINDOWS_MY:
      case WINDOWS_ROOT:
      case LOCAL_MACHINE:
        keyStore.load(null, pwd);
        break;
      default:
        break;
    }

  }


  /**
   * Set up all required references to get a valid state of this singleton.
   *
   * @param mode the mode
   * @param p12file the p 12 file
   * @param pwd the pwd
   */
  private void setUp(KeyStoreAccessMode mode, Optional<String> p12file, char[] pwd) {

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

      loadKeystore(p12file, pwd);

    } catch (NoSuchAlgorithmException | CertificateException | IOException e) {

      logger.error("Error trying to load the configured keystore", e);

    }

    try {

      keys = new HashMap<>();
      Enumeration<String> aliases = getKeystoreAliases();
      while (aliases.hasMoreElements()) {
        String element = aliases.nextElement();
        keys.put(element, keyStore.getKey(element, pwd));
      }

    } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {

      logger.error("Error getting keys from the configured keystore", e);

    }

    try {

      trustedChains = new HashMap<>();
      Enumeration<String> aliases = getKeystoreAliases();
      while (aliases.hasMoreElements()) {
        String element = aliases.nextElement();
        trustedChains.put(element, (Certificate[]) keyStore.getCertificateChain(element));
      }

    } catch (KeyStoreException e) {

      logger.error("Error getting trusted chain from the configured keystore", e);

    }

    try {

      certificates = new HashMap<>();
      Enumeration<String> aliases = getKeystoreAliases();
      while (aliases.hasMoreElements()) {
        String element = aliases.nextElement();
        certificates.put(element, (X509Certificate) keyStore.getCertificate(element));
      }

    } catch (KeyStoreException e) {

      logger.error("Error getting certificates from the configured keystore", e);

    }

  }

  /**
   * Allows perform the initial process of this singleton and it ensure to set at least a valid
   * configuration. Otherwise an IllegalStateException is thrown.
   *
   * @param mode the mode
   * @param p12file the p 12 file
   * @param pwd the pwd
   */
  public void safeInit(KeyStoreAccessMode mode, Optional<String> p12file, char[] pwd) {
    init(mode, p12file, pwd, true);
  }

  /**
   * Allows perform the initial process of this singleton and it ensure to set at least a valid
   * configuration. Otherwise an IllegalStateException is thrown.
   *
   * @param mode the mode
   */
  public void safeInit(KeyStoreAccessMode mode) {
    instance.mode = mode;
  }

  /**
   * Initialize all security objects required to sign documents.
   *
   * @param mode the mode
   * @param p12file the p 12 file
   * @param pwd the pwd
   */
  public void init(KeyStoreAccessMode mode, Optional<String> p12file, char[] pwd) {
    init(mode, p12file, pwd, false);
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

    return keys;
  }

  /**
   * Gets the private key by alias.
   *
   * @param alias the alias
   * @return the private key by alias
   */
  public Key getPrivateKeyByAlias(String alias) {

    return keys.get(alias);
  }

  /**
   * Gets the aliases.
   *
   * @return the aliases
   */
  @SuppressWarnings("unchecked")
  public Iterator<String> getAliases() {

    return keys.keySet().isEmpty() ? Collections.EMPTY_SET.iterator() : keys.keySet().iterator();
  }

  /**
   * Gets the certificate chain by alias.
   *
   * @param alias the alias
   * @return the certificate chain by alias
   */
  public Certificate[] getCertificateChainByAlias(String alias) {

    return trustedChains.get(alias);
  }

  /**
   * Gets the map trusted chain.
   *
   * @return the map trusted chain
   */
  public Map<String, Certificate[]> getMapTrustedChain() {

    return trustedChains;
  }

  /**
   * Gets the map certificate.
   *
   * @return the map certificate
   */
  public Map<String, X509Certificate> getMapCertificate() {

    return certificates;
  }

  /**
   * Gets the certificate by alias.
   *
   * @param alias the alias
   * @return the certificate by alias
   */
  public Certificate getCertificateByAlias(String alias) {

    return certificates.get(alias);
  }

  /**
   * Gets the private key by alias.
   *
   * @param alias the alias
   * @return the private key by alias
   */
  public KeyStoreAccessMode getMode() {

    return this.mode;

  }

}
