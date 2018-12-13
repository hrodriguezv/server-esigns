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

import com.consultec.esigns.core.util.InetUtility;

/**
 * The Class SecurityProvider.
 *
 * @author hrodriguez
 */
@SuppressWarnings("deprecation")
public class SecurityHelper {

	/** The provider. */
	private Provider provider;

	/** The key store. */
	private KeyStore keyStore;

	/** The mode. */
	private KeyStoreAccessMode mode;

	/** The map keys. */
	private Map<String, Key> mapKeys;

	/** The map trusted chain. */
	private Map<String, X509Certificate[]> mapTrustedChain;

	/** The map certificate. */
	private Map<String, X509Certificate> mapCertificate;

	/**
	 * Instantiates a new security provider.
	 *
	 * @param mode
	 *            the mode
	 */
	public SecurityHelper(KeyStoreAccessMode mode) {

		this.mode = mode;
		this.provider = mode.getProvider();
		Security.addProvider(provider);
	}

	/**
	 * Gets the PAdES EPES profile to set in signature object.
	 *
	 * @param digest
	 *            the digest
	 * @return the PAdES EPES profile
	 * @see #com.itextpdf.signatures.DigestAlgorithms.getAllowedDigest
	 *      DigestAlgorithms.getAllowedDigest("SHA1")
	 */
	// DigestAlgorithms.getAllowedDigest("SHA1")
	public static SignaturePolicyIdentifier getPadesEpesProfile(String digest) {

		String notExistingSignaturePolicyOid = "2.16.724.631.3.1.124.2.29.9";
		ASN1ObjectIdentifier asn1PolicyOid = DERObjectIdentifier.getInstance(
			new DERObjectIdentifier(notExistingSignaturePolicyOid));
		AlgorithmIdentifier hashAlg =
			new AlgorithmIdentifier(new ASN1ObjectIdentifier(digest));
		// indicate that the policy hash value is not known; see ETSI TS 101 733
		// V2.2.1, 5.8.1
		byte[] zeroSigPolicyHash = {
			0
		};
		DEROctetString hash = new DEROctetString(zeroSigPolicyHash);

		SignaturePolicyId signaturePolicyId = new SignaturePolicyId(
			asn1PolicyOid, new OtherHashAlgAndValue(hashAlg, hash));
		SignaturePolicyIdentifier sigPolicyIdentifier =
			new SignaturePolicyIdentifier(signaturePolicyId);
		return sigPolicyIdentifier;
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

	/**
	 * Gets the alias by common name.
	 *
	 * @param string
	 *            the string
	 * @return the alias by common name
	 */
	private String getAliasByCommonName(String string) {

		return mapCertificate.entrySet().stream().filter(entry -> {
			String alias = null;
			X500Name x500name;
			try {
				x500name =
					new JcaX509CertificateHolder(entry.getValue()).getSubject();
				if (x500name.getRDNs(BCStyle.CN).length > 0) {
					RDN cn = x500name.getRDNs(BCStyle.CN)[0];
					alias = IETFUtils.valueToString(cn.getFirst().getValue());
				}
			}
			catch (CertificateEncodingException e) {
				e.printStackTrace();
			}
			return string.equals(alias);
		}).map(Map.Entry::getKey).findFirst().orElse(null);
	}

	/**
	 * Initialize all security objects required to sign documents.
	 *
	 * @param provider
	 *            the provider
	 * @param p12file
	 *            the p 12 file
	 * @param pwd
	 *            the pwd
	 */
	public void init(
		Optional<String> provider, Optional<String> p12file, char[] pwd) {

		try {
			if (provider.isPresent()) {
				keyStore = KeyStore.getInstance(mode.getType(), provider.get());
			}
			else {
				keyStore = KeyStore.getInstance(mode.getType());
			}
		}
		catch (KeyStoreException | NoSuchProviderException e) {
			e.printStackTrace();
		}

		try {
			if (p12file.isPresent()) {
				keyStore.load(new FileInputStream(p12file.get()), pwd);
			}
			else {
				keyStore.load(null, pwd);
			}
		}
		catch (NoSuchAlgorithmException | CertificateException
						| IOException e) {
			e.printStackTrace();
		}

		try {
			mapKeys = new HashMap<>();
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String element = aliases.nextElement();
				mapKeys.put(element, keyStore.getKey(element, pwd));
			}
		}
		catch (UnrecoverableKeyException | KeyStoreException
						| NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		try {
			mapTrustedChain = new HashMap<>();
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String element = aliases.nextElement();
				mapTrustedChain.put(
					element,
					(X509Certificate[]) keyStore.getCertificateChain(element));
			}
		}
		catch (KeyStoreException e) {
			e.printStackTrace();
		}

		try {
			mapCertificate = new HashMap<>();
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String element = aliases.nextElement();
				mapCertificate.put(
					element,
					(X509Certificate) keyStore.getCertificate(element));
			}
		}
		catch (KeyStoreException e) {
			e.printStackTrace();
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
	 * @param alias
	 *            the alias
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
	 * @param alias
	 *            the alias
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
	public Map<String, X509Certificate[]> getMapTrustedChain() {

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
	 * @param alias
	 *            the alias
	 * @return the certificate by alias
	 */
	public Certificate getCertificateByAlias(String alias) {

		return mapCertificate.get(alias);
	}
}
