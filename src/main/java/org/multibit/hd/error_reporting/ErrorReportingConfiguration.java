package org.multibit.hd.error_reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * <p>DropWizard Configuration to provide the following to application:</p>
 * <ul>
 * <li>Initialisation code</li>
 * </ul>
 *
 * @since 0.0.1
 *         
 */
public class ErrorReportingConfiguration extends Configuration {

  @Valid
  @NotNull
  @JsonProperty
  private boolean production = true;

  public boolean isProduction() {
    return production;
  }
}
