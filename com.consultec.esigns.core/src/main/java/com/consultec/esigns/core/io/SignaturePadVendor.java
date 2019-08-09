package com.consultec.esigns.core.io;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The Enum SignaturePadVendor.
 *
 * @author hrodriguez
 */
@Getter
@AllArgsConstructor
public enum SignaturePadVendor {

  /** The wacom. */
  WACOM("WAC"),
  /** The topaz. */
  TOPAZ("AHA");

  /** The vendor ID. */
  private String vendorPrefix;

  /**
   * From string.
   *
   * @param text the text
   * @return the signature pad vendor
   */
  public static SignaturePadVendor fromString(String text) {

    for (SignaturePadVendor b : SignaturePadVendor.values()) {

      if (b.vendorPrefix.equalsIgnoreCase(text)) {
        return b;
      }

    }

    return null;

  }

}
