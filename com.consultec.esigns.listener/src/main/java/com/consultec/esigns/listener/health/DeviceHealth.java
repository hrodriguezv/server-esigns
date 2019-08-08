package com.consultec.esigns.listener.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

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

    return true;

  }

}
