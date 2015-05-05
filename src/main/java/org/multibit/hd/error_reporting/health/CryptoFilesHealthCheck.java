package org.multibit.hd.error_reporting.health;

import com.yammer.metrics.core.HealthCheck;
import org.multibit.hd.error_reporting.ErrorReportingService;

/**
 * <p>HealthCheck to provide the following to application:</p>
 * <ul>
 * <li>Provision of checks against the crypto files (quick encrypt/decrypt operation)</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class CryptoFilesHealthCheck extends HealthCheck {

  public CryptoFilesHealthCheck() {
    super("Error Reporting environment health check");
  }

  @Override
  protected Result check() throws Exception {

    try {
      ErrorReportingService.verifyCryptoFiles();
    } catch (Exception e) {
      return Result.unhealthy(e);
    }

    return Result.healthy();
  }
}