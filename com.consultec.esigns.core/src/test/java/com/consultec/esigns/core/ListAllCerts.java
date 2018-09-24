
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

import com.pheox.jcapi.JCAPIProvider;
import com.pheox.jcapi.JCAPIUtil;

/**
 * This example will list all certificates stored in all available MS CAPI
 * system (certificate) stores.
 */
public class ListAllCerts {

	public static void main(String[] args) {

		try {
			Security.addProvider(new JCAPIProvider());

			KeyStore ks = KeyStore.getInstance("msks", "JCAPI");
			ks.load(null, null);
			String alias = null;
			int i = 0;
			// Print out all certificates found in all stores.
			for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();) {
				alias = (String) e.nextElement();
				System.out.println("Alias: " + alias);
				System.out.println(
					"Certificate friendly name: " +
						JCAPIUtil.getCertificateFriendlyName(alias));
				System.out.println(
					"\nCertificate: \n" + ((X509Certificate) ks.getCertificate(
						alias)).getSubjectX500Principal().getName());
				X500Name x500name = new JcaX509CertificateHolder(
					(X509Certificate) ks.getCertificate(alias)).getSubject();
				if (x500name.getRDNs(BCStyle.CN).length > 0) {
					RDN cn = x500name.getRDNs(BCStyle.CN)[0];
					System.err.println(
						IETFUtils.valueToString(cn.getFirst().getValue()));
				}
				i++;
			}
			System.out.println("The total number of certificates are: " + i);
			System.out.println(
				"The following key/certificate stores was searched: ");
			String[] certStores = JCAPIUtil.getCertStoreNames();
			for (i = 0; i < certStores.length; i++)
				System.out.println(certStores[i]);
		}
		catch (Throwable t) {
			System.err.println("Example program failed.");
			t.printStackTrace();
		}
	}
}
