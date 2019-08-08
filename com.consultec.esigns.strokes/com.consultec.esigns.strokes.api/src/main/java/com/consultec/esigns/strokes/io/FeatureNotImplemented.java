/**
 * 
 */
package com.consultec.esigns.strokes.io;

/**
 * The Class FeatureNotImplemented.
 *
 * @author hrodriguez
 */
public class FeatureNotImplemented extends RuntimeException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 7750699895543167627L;

  /** The detailed cause. */
  @SuppressWarnings("unused")
  private String detailedCause;

  /**
   * Instantiates a new feature not implemented.
   *
   * @param string the string
   */
  public FeatureNotImplemented(String string) {
    this.detailedCause = string;
  }

}
