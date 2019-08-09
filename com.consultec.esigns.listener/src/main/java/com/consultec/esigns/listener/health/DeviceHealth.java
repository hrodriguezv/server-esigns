package com.consultec.esigns.listener.health;

import java.io.IOException;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.consultec.esigns.core.io.SignaturePadVendor;
import com.consultec.esigns.core.util.WMICUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeviceHealth implements HealthIndicator {

  @Override
  public Health health() {

    if (isDeviceConnected()) {

      return Health.up().build();

    }

    return Health.down().build();

  }

  private boolean isDeviceConnected() {

    try {
      //this solution was designed to use a WACOM device by default
      return WMICUtil.getRawDevicesConnected().stream()
          .anyMatch(str -> str.equals(SignaturePadVendor.WACOM.getVendorID()));

    } catch (IOException e) {

      log.error("Error getting the connected devices", e);

    }

    return false;

  }

}
