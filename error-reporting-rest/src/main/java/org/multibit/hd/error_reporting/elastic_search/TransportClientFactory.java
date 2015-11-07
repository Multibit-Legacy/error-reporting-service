package org.multibit.hd.error_reporting.elastic_search;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.util.Map;


/**
 * <p>Factory to provide the following to Elasticsearch client:</p>
 * <ul>
 * <li>Creation of client instances</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class TransportClientFactory {

  /**
   * Utilities have private constructors
   */
  private TransportClientFactory() {
  }

  /**
   * @return A new client
   *
   * @throws IOException If something goes wrong
   */
  public static TransportClient newClient(
    Map<String, String> settings,
    String clientTransportHost,
    Integer clientTransportPort,
    boolean disabled
  ) throws IOException {

    if (disabled) {
      throw new IOException("ES is disabled. Check 'es.disable' property");
    }

    Settings immutableSettings = ImmutableSettings
      .settingsBuilder()
      .put(settings)
      .build();

    TransportClient client = new TransportClient(immutableSettings);

    System.out.print(" ("+clientTransportHost+":"+clientTransportPort+") ");
    client.addTransportAddress(new InetSocketTransportAddress(clientTransportHost,clientTransportPort));

    verifyConnection(client);

    return client;
  }

  private static void verifyConnection(TransportClient client) throws IOException {

    ImmutableList<DiscoveryNode> nodes = client.connectedNodes();
    if (nodes.isEmpty()) {
      throw new IOException("No nodes available. Verify ES is running.");
    }

  }

}