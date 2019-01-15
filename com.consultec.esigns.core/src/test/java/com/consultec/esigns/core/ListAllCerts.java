
package com.consultec.esigns.core;

import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pheox.jcapi.JCAPIProvider;
import com.pheox.jcapi.JCAPIUtil;

/**
 * This example will list all certificates stored in all available MS CAPI
 * system (certificate) stores.
 */
public class ListAllCerts {

	/** The Constant logger. */
	private static final Logger logger =
		LoggerFactory.getLogger(ListAllCerts.class);

	public static void main(String[] args) {

		try {
			Security.addProvider(new JCAPIProvider());

			KeyStore ks = KeyStore.getInstance("msks", "JCAPI");
			ks.load(null, null);
			String alias = null;
			int i = 0;
			// Print out all certificates found in all stores.
			for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
				alias = e.nextElement();
				logger.info("Alias: " + alias);
				logger.info(
					"Certificate friendly name: " +
						JCAPIUtil.getCertificateFriendlyName(alias));
				logger.info(
					"\nCertificate: \n" + ((X509Certificate) ks.getCertificate(
						alias)).getSubjectX500Principal().getName());
				X500Name x500name = new JcaX509CertificateHolder(
					(X509Certificate) ks.getCertificate(alias)).getSubject();
				if (x500name.getRDNs(BCStyle.CN).length > 0) {
					RDN cn = x500name.getRDNs(BCStyle.CN)[0];
					logger.info(
						IETFUtils.valueToString(cn.getFirst().getValue()));
				}
				i++;
			}
			logger.info("The total number of certificates are: " + i);
			logger.info("The following key/certificate stores was searched: ");
			String[] certStores = JCAPIUtil.getCertStoreNames();
			for (i = 0; i < certStores.length; i++)
				logger.info(certStores[i]);
		}
		catch (Exception t) {
			logger.error("Example program failed." + t.getMessage());
		}
	}
}
