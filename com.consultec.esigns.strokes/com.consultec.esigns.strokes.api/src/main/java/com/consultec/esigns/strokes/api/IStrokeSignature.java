/**
 * 
 */
package com.consultec.esigns.strokes.api;

import com.consultec.esigns.strokes.SignaturePadVendor;

/**
 * @author hrodriguez
 *
 */
public interface IStrokeSignature {

  int sign();

  String getEncodedImage();

  byte[] getImage();

  String getEncodedSign();

  void writeImageFile(String dest);

  SignaturePadVendor getVendor();

  void setParameters(String author, String reason, String location);

}
