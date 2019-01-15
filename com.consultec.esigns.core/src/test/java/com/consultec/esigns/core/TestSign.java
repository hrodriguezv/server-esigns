
package com.consultec.esigns.core;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consultec.esigns.core.security.KeyStoreAccessMode;
import com.consultec.esigns.core.security.SecurityHelper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.signatures.CrlClientOnline;
import com.itextpdf.signatures.ICrlClient;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.LtvVerification;
import com.itextpdf.signatures.OcspClientBouncyCastle;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.TSAClientBouncyCastle;

public class TestSign {

	/** The Constant logger. */
	private static final Logger logger =
		LoggerFactory.getLogger(TestSign.class);

	protected static final String BASEPATH = "";
	protected static final String KEYSTORE = BASEPATH + "keystore.p12";
	protected static final char[] KEY = "123456".toCharArray();
	protected static final String PDFIN =
		BASEPATH + "Holamundo-variaspaginas.pdf";
	protected static final String PDFOUT = BASEPATH + "file-signed.pdf";
	protected static final String BACK_IMG = BASEPATH + "sig.png";

	public static void main(String[] args)
		throws Exception {

		String urlTSA = "http://as-demo.bit4id.org/smartengine/tsa";

		Certificate[] signChain = null;

		Optional<String> nill = Optional.ofNullable(null);
		KeyStoreAccessMode mode = KeyStoreAccessMode.WINDOWS_MY;

		SecurityHelper helper = new SecurityHelper(mode);

		helper.init(nill, nill, null);

		String alias = helper.getAlias();
		signChain = helper.getCertificateChainByAlias(alias);

		ITSAClient tsaClient = new TSAClientBouncyCastle(urlTSA);
		IOcspClient ocspClient = new OcspClientBouncyCastle(null);
		List<ICrlClient> listCrl = new ArrayList<>();
		ICrlClient crl = new CrlClientOnline(signChain);
		listCrl.add(crl);
		addLtv(PDFOUT, PDFOUT + ".1", ocspClient, crl, tsaClient);
	}

	private static void addLtv(
		String src, String dest, IOcspClient ocsp, ICrlClient crl,
		ITSAClient tsa) {

		
		try (FileOutputStream fos = new FileOutputStream(dest)){
			PdfReader r = new PdfReader(src);
			PdfDocument pdfDoc =
				new PdfDocument(new PdfReader(src), new PdfWriter(dest));
			PdfSigner ps = new PdfSigner(r, fos, true);

			LtvVerification v = new LtvVerification(pdfDoc);
			SignatureUtil signatureUtil = new SignatureUtil(pdfDoc);

			List<String> names = signatureUtil.getSignatureNames();
			String sigName = names.get(names.size() - 1);
			Provider p = new BouncyCastleProvider();
			Security.addProvider(p);
			PdfPKCS7 pkcs7 =
				signatureUtil.verifySignature(sigName, p.getName());

			if (pkcs7.isTsp()) {
				v.addVerification(
					sigName, ocsp, crl,
					LtvVerification.CertificateOption.WHOLE_CHAIN,
					LtvVerification.Level.CRL,
					LtvVerification.CertificateInclusion.YES);
			}
			else {
				for (String name : names) {
					v.addVerification(
						name, ocsp, crl,
						LtvVerification.CertificateOption.WHOLE_CHAIN,
						LtvVerification.Level.OCSP,
						LtvVerification.CertificateInclusion.YES);
					v.merge();
				}
			}
			ps.timestamp(tsa, null);
		}
		catch (IOException | GeneralSecurityException e) {
			logger.error(e.getLocalizedMessage());
		}
	}
}
