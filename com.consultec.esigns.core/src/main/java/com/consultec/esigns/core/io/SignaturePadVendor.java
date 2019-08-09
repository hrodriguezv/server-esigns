package com.consultec.esigns.core.io;

/**
 * The Enum SignaturePadVendor.
 *
 * @author hrodriguez
 */
public enum SignaturePadVendor {

  /** The wacom. */
  WACOM("WAC1047"),
  /** The topaz. */
  TOPAZ("AHA0001");

  /** The vendor ID. */
  private String vendorID;

  /**
   * Instantiates a new signature pad vendor.
   *
   * @param id the vendor
   */
  SignaturePadVendor(String id) {
    this.vendorID = id;
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
