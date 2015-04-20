package org.multibit.hd.error_reporting.health;

import com.yammer.metrics.core.HealthCheck;

/**
 * <p>HealthCheck to provide the following to application:</p>
 * <ul>
 * <li>Provision of checks against the current Matcher</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ErrorReportingHealthCheck extends HealthCheck {

  public ErrorReportingHealthCheck() {
    super("Matcher health check");
  }

  @Override
  protected Result check() throws Exception {

    // TODO Determine best way of detecting a Matcher failure

    return Result.healthy();
  }
}