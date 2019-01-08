/**
 * 
 */

package com.consultec.esigns.core.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.consultec.esigns.core.security.SecurityHelper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.signatures.CrlClientOnline;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ICrlClient;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.LtvVerification;
import com.itextpdf.signatures.OcspClientBouncyCastle;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.TSAClientBouncyCastle;

/**
 * The Class PDFSignatureUtil.
 *
 * @author hrodriguez
 */
public class PDFSignatureUtil {

	/**
	 * Sign adding pades epes profile.
	 *
	 * @param digest
	 *            the digest
	 * @param certificate
	 *            the certificate
	 * @param pks
	 *            the pks
	 * @param signChain
	 *            the sign chain
	 * @param pdfInputPath
	 *            the pdf input path
	 * @param pdfOutputPath
	 *            the pdf output path
	 * @param tsaServerURL
	 *            the tsa server URL
	 * @param reason
	 *            the reason
	 * @param location
	 *            the location
	 * @param userName
	 *            the user name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GeneralSecurityException
	 *             the general security exception
	 */
	public static void signAddingPadesEpesProfile(
		IExternalDigest digest, Certificate certificate, IExternalSignature pks,
		Certificate[] signChain, String pdfInputPath, String pdfOutputPath,
		String tsaServerURL, Optional<String> reason, Optional<String> location,
		Optional<String> userName)
		throws IOException, GeneralSecurityException {

		String alg = DigestAlgorithms.getAllowedDigest("SHA1");
		SignaturePolicyIdentifier sigPolicyIdentifier =
			SecurityHelper.getPadesEpesProfile(alg);
		signApproval(
			digest, certificate, pks, signChain, pdfInputPath, pdfOutputPath,
			sigPolicyIdentifier, tsaServerURL, reason, location, userName);
	}

	/**
	 * Sign approval.
	 *
	 * @param digest
	 *            the digest
	 * @param certificate
	 *            the certificate
	 * @param pks
	 *            the pks
	 * @param signChain
	 *            the sign chain
	 * @param pdfInput
	 *            the pdf input
	 * @param outFileName
	 *            the out file name
	 * @param sigPolicyInfo
	 *            the sig policy info
	 * @param tsaClientURL
	 *            the tsa client URL
	 * @param reason
	 *            the reason
	 * @param location
	 *            the location
	 * @param userName
	 *            the user name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GeneralSecurityException
	 *             the general security exception
	 */
	public static void signApproval(
		IExternalDigest digest, Certificate certificate, IExternalSignature pks,
		Certificate[] signChain, String pdfInput, String outFileName,
		SignaturePolicyIdentifier sigPolicyInfo, String tsaClientURL,
		Optional<String> reason, Optional<String> location,
		Optional<String> userName)
		throws IOException, GeneralSecurityException {

		PdfReader reader = new PdfReader(pdfInput);
		PdfSigner signer =
			new PdfSigner(reader, new FileOutputStream(outFileName), false);
		signer.setFieldName("Signature1");
		PdfSignatureAppearance appearance = signer.getSignatureAppearance();

		if (reason.isPresent()) {
			appearance.setReason(reason.get());
		}
		if (location.isPresent()) {
			appearance.setLocation(location.get());
		}
		if (userName.isPresent()) {
			appearance.setLayer2Text(userName.get());
		}
		appearance.setCertificate(certificate);

		// add tsa stamp
		ITSAClient tsaClient = new TSAClientBouncyCastle(tsaClientURL);
		IOcspClient ocspClient = new OcspClientBouncyCastle(null);
		List<ICrlClient> listCrl = new ArrayList<>();
		ICrlClient crl = new CrlClientOnline(signChain);
		listCrl.add(crl);

		if (sigPolicyInfo == null) {
			signer.signDetached(
				digest, pks, signChain, listCrl, ocspClient, tsaClient, 0,
				PdfSigner.CryptoStandard.CADES);
		}
		else {
			signer.signDetached(
				digest, pks, signChain, listCrl, ocspClient, tsaClient, 0,
				PdfSigner.CryptoStandard.CADES, sigPolicyInfo);
		}
	}

	/**
	 * Basic check signed doc.
	 *
	 * @param filePath
	 *            the file path
	 * @param signatureName
	 *            the signature name
	 * @return true, if successful
	 * @throws GeneralSecurityException
	 *             the general security exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean basicCheckSignedDoc(
		String filePath, String signatureName)
		throws GeneralSecurityException, IOException {

		PdfDocument outDocument = new PdfDocument(new PdfReader(filePath));
		SignatureUtil sigUtil = new SignatureUtil(outDocument);
		PdfPKCS7 pdfPKCS7 = sigUtil.verifySignature(signatureName);
		outDocument.close();
		return pdfPKCS7.verify();
	}

	/**
	 * Adds the ltv stamp.
	 *
	 * @param src the src
	 * @param dest the dest
	 * @param ocsp the ocsp
	 * @param crl the crl
	 * @param tsa the tsa
	 * @throws Exception the exception
	 */
	public static void addLtvStamp(
		String src, String dest, IOcspClient ocsp, ICrlClient crl,
		ITSAClient tsa)
		throws Exception {

		PdfReader r = new PdfReader(src);
		FileOutputStream fos = new FileOutputStream(dest);
		PdfDocument pdfDoc =
			new PdfDocument(new PdfReader(src), new PdfWriter(dest));
		PdfSigner ps = new PdfSigner(r, fos, true);

		LtvVerification v = new LtvVerification(pdfDoc);
		SignatureUtil signatureUtil = new SignatureUtil(pdfDoc);

		List<String> names = signatureUtil.getSignatureNames();
		String sigName = names.get(names.size() - 1);
		Provider p = new BouncyCastleProvider();
		Security.addProvider(p);
		PdfPKCS7 pkcs7 = signatureUtil.verifySignature(sigName, p.getName());

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
}
