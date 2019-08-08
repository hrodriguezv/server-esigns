/**
 * 
 */
package com.consultec.esigns.strokes;

/**
 * The Enum SignaturePadVendor.
 *
 * @author hrodriguez
 */
public enum SignaturePadVendor {

  /** The wacom. */
  WACOM("WAC1047", "com.consultec.esigns.strokes.impl.WacomStrokesSignatureImpl"),
  /** The topaz. */
  TOPAZ("AHA0001", "");

  /** The vendor ID. */
  private String vendorID;

  /** The fully qualified name. */
  private String fullyQualifiedName;

  /**
   * Instantiates a new signature pad vendor.
   *
   * @param vendor the vendor
   * @param clazzName the clazz name
   */
  SignaturePadVendor(String vendor, String clazzName) {
    this.vendorID = vendor;
    this.fullyQualifiedName = clazzName;
  }

  /**
   * Gets the vendor ID.
   *
   * @return the vendor ID
   */
  public String getVendorID() {
    return vendorID;
  }

  /**
   * Gets the fully qualified name.
   *
   * @return the fully qualified name
   */
  public String getFullyQualifiedName() {
    return fullyQualifiedName;
  }

  /**
   * From string.
   *
   * @param text the text
   * @return the signature pad vendor
   */
  public static SignaturePadVendor fromString(String text) {

    for (SignaturePadVendor b : SignaturePadVendor.values()) {

      if (b.vendorID.equalsIgnoreCase(text)) {
        return b;
      }

    }

    return null;

  }
}
