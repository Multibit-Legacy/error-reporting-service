package org.multibit.hd.error_reporting;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

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

  @NotEmpty
  @JsonProperty
  private String elasticsearchHost = "localhost";

  @NotEmpty
  @JsonProperty
  private String elasticsearchPort = "9300";

  @NotEmpty
  @JsonProperty
  private String clusterName = "elasticsearch";

  @Valid
  @NotNull
  @JsonProperty
  private boolean sendEmail = false; // Default is false for testing

  public boolean isProduction() {
    return production;
  }

  public String getElasticsearchHost() {
    return elasticsearchHost;
  }

  public void setElasticsearchHost(String elasticsearchHost) {
    this.elasticsearchHost = elasticsearchHost;
  }

  public String getElasticsearchPort() {
    return elasticsearchPort;
  }

  public void setElasticsearchPort(String elasticsearchPort) {
    this.elasticsearchPort = elasticsearchPort;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public boolean isSendEmail() {
    return sendEmail;
  }

  public void setSendEmail(boolean sendEmail) {
    this.sendEmail = sendEmail;
  }

}
