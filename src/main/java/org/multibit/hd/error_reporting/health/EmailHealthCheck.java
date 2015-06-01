package org.multibit.hd.error_reporting.health;

/**
 * <p>HealthCheck to provide the following to application:</p>
 * <ul>
 * <li>Provision of checks against a given Configuration property </li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */

import com.yammer.metrics.core.HealthCheck;
import org.multibit.hd.error_reporting.core.email.Emails;

public class EmailHealthCheck extends HealthCheck {

  public EmailHealthCheck() {
    super("SMTP gateway health check");
  }

  @Override
  protected Result check() throws Exception {
    try {
      Emails.sendSupportEmail("Health check email (Error Reporting Service) - please delete");
    } catch (IllegalStateException e) {
      return Result.unhealthy(e.getMessage());
    }

    return Result.healthy();
  }
}