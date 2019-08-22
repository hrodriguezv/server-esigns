package com.consultec.esigns.strokes.impl;

import com.consultec.esigns.strokes.SignaturePadVendor;
import com.consultec.esigns.strokes.api.IStrokeSignatureLicensed;
import com.consultec.esigns.strokes.io.FeatureNotImplemented;
import com.florentis.signature.DynamicCapture;
import com.florentis.signature.SigCtl;
import com.florentis.signature.SigObj;

/**
 * The Class WacomStrokesSignatureImpl.
 *
 * @author hrodriguez
 */
public class WacomStrokesSignatureImpl implements IStrokeSignatureLicensed {

  /** The sig ctl. */
  private SigCtl sigCtl;

  /** The dc. */
  private DynamicCapture dc;

  /** The sig. */
  private SigObj sig;

  /** The reason. */
  private String reason;

  /** The author. */
  private String author;

  /** The location. */
  @SuppressWarnings("unused")
  private String location;

  /**
   * Instantiates a new wacom strokes signature impl.
   */
  public WacomStrokesSignatureImpl() {

    sigCtl = new SigCtl();

    dc = new DynamicCapture();

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#sign()
   */
  public int sign() {
    int rc = dc.capture(this.sigCtl, this.author != null ? this.author : "N/A",
      this.reason != null ? this.reason : "N/A", null, null);
    this.sig = this.sigCtl.signature();
    return rc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#getEncodedSign()
   */
  public String getEncodedSign() {

    String s = this.sig.sigText();
    return s;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#getEncodedImage()
   */
  public String getEncodedImage() {

    throw new FeatureNotImplemented("This feature have not been implemented yet");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#getImage()
   */
  public byte[] getImage() {

    throw new FeatureNotImplemented("This feature have not been implemented yet");
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#writeImageFile(java. lang. String)
   */
  public void writeImageFile(String dest) {

    int flags = SigObj.outputFilename | SigObj.color32BPP | SigObj.backgroundTransparent
        | SigObj.encodeData | SigObj.renderRelative;
    this.sig.renderBitmap(dest, -200, -200, "image/png", 1.0f, 0xff0000, 0xffffff, 0.0f, 0.0f,
      flags);
  }

  /**
   * Sets the reason.
   *
   * @param reason the new reason
   */
  public void setReason(String reason) {

    this.reason = reason;
  }

  /**
   * Sets the author.
   *
   * @param author the new author
   */
  public void setAuthor(String author) {

    this.author = author;
  }

  /**
   * Sets the location.
   *
   * @param location the new location
   */
  public void setLocation(String location) {

    this.location = location;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#getVendor()
   */
  public SignaturePadVendor getVendor() {

    return SignaturePadVendor.WACOM;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.consultec.esigns.strokes.api.IStrokeSignature#setParameters(java.lang .String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void setParameters(String author, String reason, String location) {
    this.author = author;
    this.location = location;
    this.reason = reason;
  }

  @Override
  public void setLicense(String key) {
    sigCtl.licence(key);
  }

}
