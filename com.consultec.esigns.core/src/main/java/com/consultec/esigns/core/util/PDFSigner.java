package com.consultec.esigns.core.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.asn1.esf.SignaturePolicyIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consultec.esigns.core.security.SecurityManager;
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
 * The Class PDFSigner.
 *
 * @author hrodriguez
 */
public class PDFSigner {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(PDFSigner.class);

  /** The digest. */
  private final IExternalDigest digest;

  /** The pks. */
  private final IExternalSignature pks;

  /** The certificate. */
  private final Certificate certificate;

  /** The trusted signature chain. */
  private final Certificate[] trustedSignatureChain;

  /** The pdf input path. */
  private final String pdfInputPath;

  /** The pdf output path. */
  private final String pdfOutputPath;

  /** The tsa server URL. */
  private final Optional<String> tsaServerURL;

  /** The reason. */
  private final Optional<String> reason;

  /** The location. */
  private final Optional<String> location;

  /** The user name. */
  private final Optional<String> userName;

  /** The signature name. */
  private static final String SIGNATURE_NAME_PLACEHOLDER = "Signature1";

  /**
   * The Class Builder.
   */
  public static class Builder {

    /** The digest. */
    // required parameters
    private IExternalDigest digest;

    /** The pks. */
    private IExternalSignature pks;

    /** The certificate. */
    private Certificate certificate;

    /** The sign chain. */
    private Certificate[] signChain;

    /** The pdf input path. */
    private String pdfInputPath;

    /** The pdf output path. */
    private String pdfOutputPath;

    /** The tsa server URL. */
    private Optional<String> tsaServerURL = Optional.empty();

    /** The reason. */
    // optional parameters
    private Optional<String> reason = Optional.empty();

    /** The location. */
    private Optional<String> location = Optional.empty();

    /** The user name. */
    private Optional<String> userName = Optional.empty();

    /**
     * Instantiates a new builder.
     *
     * @param digest the digest
     * @param pks the pks
     * @param certificate the certificate
     * @param signChain the sign chain
     * @param pdfInputPath the pdf input path
     * @param pdfOutputPath the pdf output path
     * @param tsaServerURL the tsa server URL
     */
    public Builder(IExternalDigest digest, IExternalSignature pks, Certificate certificate,
        Certificate[] signChain, String pdfInputPath, String pdfOutputPath) {

      this.digest = digest;
      this.pks = pks;
      this.certificate = certificate;
      this.signChain = signChain;
      this.pdfInputPath = pdfInputPath;
      this.pdfOutputPath = pdfOutputPath;

    }

    /**
     * Location.
     *
     * @param r the r
     * @return the builder
     */
    public Builder location(Optional<String> r) {

      location = r;
      return this;

    }

    /**
     * Reason.
     *
     * @param r the r
     * @return the builder
     */
    public Builder reason(Optional<String> r) {

      reason = r;
      return this;

    }

    /**
     * tsa URL
     * 
     * @param r
     * @return
     */
    public Builder tsaServerURL(Optional<String> r) {

      tsaServerURL = r;
      return this;

    }

    /**
     * User name.
     *
     * @param r the r
     * @return the builder
     */
    public Builder userName(Optional<String> r) {

      userName = r;
      return this;

    }

    /**
     * Builds the.
     *
     * @return the PDF signer
     */
    public PDFSigner build() {
      return new PDFSigner(this);
    }

  }

  /**
   * Instantiates a new PDF signer.
   *
   * @param builder the builder
   */
  private PDFSigner(Builder builder) {

    digest = builder.digest;
    pks = builder.pks;
    certificate = builder.certificate;
    trustedSignatureChain = builder.signChain;
    pdfInputPath = builder.pdfInputPath;
    pdfOutputPath = builder.pdfOutputPath;
    tsaServerURL = builder.tsaServerURL;
    reason = builder.reason;
    location = builder.location;
    userName = builder.userName;

  }

  /**
   * Sign the pre-configured input file.
   *
   * @throws GeneralSecurityException the general security exception
   */
  public Boolean sign() throws GeneralSecurityException {

    try (OutputStream output = new FileOutputStream(pdfOutputPath);
        PdfReader input = new PdfReader(pdfInputPath);) {

      PdfSigner signer = new PdfSigner(input, output, false);
      signer.setFieldName(SIGNATURE_NAME_PLACEHOLDER);
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

      ITSAClient tsaClient = null;

      if (tsaServerURL.isPresent()) {

        // add tsa stamp
        tsaClient = new TSAClientBouncyCastle(tsaServerURL.get());

      }

      IOcspClient ocspClient = new OcspClientBouncyCastle(null);
      List<ICrlClient> listCrl = new ArrayList<>();
      ICrlClient crl = new CrlClientOnline(trustedSignatureChain);
      listCrl.add(crl);

      SignaturePolicyIdentifier sigPolicyIdentifier =
          SecurityManager.getPadesEpesProfile(DigestAlgorithms.getAllowedDigest("SHA1"));

      if (sigPolicyIdentifier == null) {

        signer.signDetached(digest, pks, trustedSignatureChain, listCrl, ocspClient, tsaClient, 0,
          PdfSigner.CryptoStandard.CADES);

      } else {

        signer.signDetached(digest, pks, trustedSignatureChain, listCrl, ocspClient, tsaClient, 0,
          PdfSigner.CryptoStandard.CADES, sigPolicyIdentifier);

      }

      return true;

    } catch (IOException except) {
      logger.error("Error trying to sign a document " + except.getMessage());;
    }

    return false;

  }

  /**
   * Basic check signed doc.
   *
   * @return true, if successful
   * @throws GeneralSecurityException the general security exception
   */
  public boolean basicCheckSignedDoc() throws GeneralSecurityException {

    try (PdfDocument outDocument = new PdfDocument(new PdfReader(pdfOutputPath))) {

      SignatureUtil sigUtil = new SignatureUtil(outDocument);
      PdfPKCS7 pdfPKCS7 = sigUtil.verifySignature(SIGNATURE_NAME_PLACEHOLDER);

      return pdfPKCS7.verify();

    } catch (IOException except) {
      logger.error("Error trying to sign a document " + except.getMessage());;
    }

    return false;
  }

}
