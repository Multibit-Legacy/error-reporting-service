package org.multibit.hd.error_reporting.health;

import com.yammer.metrics.core.HealthCheck;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.client.Client;

/**
 * <p>HealthCheck to provide the following to application:</p>
 * <ul>
 * <li>Provision of checks against the current environment</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class ESHealthCheck extends HealthCheck {

  private Client client;

  public ESHealthCheck(String name, Client client) {
    super(name);
    this.client = client;
  }

  @Override
  protected Result check() throws Exception {

    // Perform a check against the configured cluster
    ClusterHealthResponse clusterHealthResponse = client
      .admin()
      .cluster()
      .health(new ClusterHealthRequest())
      .actionGet();

    switch (clusterHealthResponse.getStatus()) {
      case GREEN:
        return HealthCheck.Result.healthy();
      case YELLOW:
        return HealthCheck.Result.unhealthy("YELLOW. Replication may not be complete or new nodes are present");
      case RED:
      default:
        return HealthCheck.Result.unhealthy("RED. Something is very wrong with the cluster.%n%s", clusterHealthResponse);

    }
  }
}