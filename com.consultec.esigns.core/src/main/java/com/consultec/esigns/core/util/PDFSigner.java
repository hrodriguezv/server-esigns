/**
 * 
 */

package com.consultec.esigns.core.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;

import com.consultec.esigns.core.security.SecurityHelper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.CrlClientOnline;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ICrlClient;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.IOcspClient;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.OcspClientBouncyCastle;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.signatures.TSAClientBouncyCastle;

/**
 * @author hrodriguez
 */
public class PDFSigner {

	private final IExternalDigest digest;
	private final IExternalSignature pks;
	private final Certificate certificate;
	private final Certificate[] signChain;
	private final String pdfInputPath;
	private final String pdfOutputPath;
	private final String tsaServerURL;
	private final Optional<String> reason;
	private final Optional<String> location;
	private final Optional<String> userName;

	private final String SIGNATURE_NAME = "Signature1";

	public static class Builder {

		// required parameters
		private IExternalDigest digest;
		private IExternalSignature pks;
		private Certificate certificate;
		private Certificate[] signChain;
		private String pdfInputPath;
		private String pdfOutputPath;
		private String tsaServerURL;
		// optional parameters
		private Optional<String> reason = Optional.empty();
		private Optional<String> location = Optional.empty();
		private Optional<String> userName = Optional.empty();

		public Builder(
			IExternalDigest digest, IExternalSignature pks,
			Certificate certificate, Certificate[] signChain,
			String pdfInputPath, String pdfOutputPath, String tsaServerURL) {

			this.digest = digest;
			this.pks = pks;
			this.certificate = certificate;
			this.signChain = signChain;
			this.pdfInputPath = pdfInputPath;
			this.pdfOutputPath = pdfOutputPath;
			this.tsaServerURL = tsaServerURL;
		}

		public Builder location(Optional<String> r) {

			location = r;
			return this;
		}

		public Builder reason(Optional<String> r) {

			reason = r;
			return this;
		}

		public Builder userName(Optional<String> r) {

			userName = r;
			return this;
		}

		public PDFSigner build() {

			return new PDFSigner(this);
		}
	}

	private PDFSigner(Builder builder) {

		digest = builder.digest;
		pks = builder.pks;
		certificate = builder.certificate;
		signChain = builder.signChain;
		pdfInputPath = builder.pdfInputPath;
		pdfOutputPath = builder.pdfOutputPath;
		tsaServerURL = builder.tsaServerURL;
		reason = builder.reason;
		location = builder.location;
		userName = builder.userName;
	}

	public void sign()
		throws IOException, GeneralSecurityException {

		PdfReader reader = new PdfReader(pdfInputPath);
		PdfSigner signer =
			new PdfSigner(reader, new FileOutputStream(pdfOutputPath), false);
		signer.setFieldName(SIGNATURE_NAME);
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
		ITSAClient tsaClient = new TSAClientBouncyCastle(tsaServerURL);
		IOcspClient ocspClient = new OcspClientBouncyCastle(null);
		List<ICrlClient> listCrl = new ArrayList<>();
		ICrlClient crl = new CrlClientOnline(signChain);
		listCrl.add(crl);

		SignaturePolicyIdentifier sigPolicyIdentifier =
			SecurityHelper.getPadesEpesProfile(
				DigestAlgorithms.getAllowedDigest("SHA1"));

		if (sigPolicyIdentifier == null) {
			signer.signDetached(
				digest, pks, signChain, listCrl, ocspClient, tsaClient, 0,
				PdfSigner.CryptoStandard.CADES);
		}
		else {
			signer.signDetached(
				digest, pks, signChain, listCrl, ocspClient, tsaClient, 0,
				PdfSigner.CryptoStandard.CADES, sigPolicyIdentifier);
		}
	}

	public boolean basicCheckSignedDoc()
		throws GeneralSecurityException, IOException {

		PdfDocument outDocument = new PdfDocument(new PdfReader(pdfOutputPath));
		SignatureUtil sigUtil = new SignatureUtil(outDocument);
		PdfPKCS7 pdfPKCS7 = sigUtil.verifySignature(SIGNATURE_NAME);
		outDocument.close();
		return pdfPKCS7.verify();
	}
}
