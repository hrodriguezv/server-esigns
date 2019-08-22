/**
 * 
 */
package com.consultec.esigns.strokes;

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
  WACOM("WAC1047", "com.consultec.esigns.strokes.impl.WacomStrokesSignatureImpl"),
  /** The topaz. */
  TOPAZ("AHA0001", "");

  /** The vendor ID. */
  private String vendorID;

  /** The fully qualified name. */
  private String fullyQualifiedName;

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