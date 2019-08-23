package com.consultec.esigns.listener.health;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.consultec.esigns.core.io.SignaturePadVendor;
import com.consultec.esigns.core.util.WMICUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DeviceHealth implements HealthIndicator {

  private static final String WINDOWS_OS_NAME_PREFIX = "Windows 7";

  @Override
  public Health health() {

    if (isDeviceConnected()) {

      return Health.up().build();

    }

    return Health.down().build();

  }

  private boolean isDeviceConnected() {

    try {

      String osName = System.getProperty("os.name");

      if (StringUtils.equalsIgnoreCase(osName, WINDOWS_OS_NAME_PREFIX)) {

        return true;

      }

      List<String> devices = WMICUtil.getRawDevicesConnected();

      // this solution was designed to use a WACOM device by default
      return devices.stream()
          .anyMatch(str -> str.startsWith(SignaturePadVendor.WACOM.getVendorPrefix()));

    } catch (IOException e) {

      log.error("Error getting the connected devices", e);

    }

    return false;

  }

}
