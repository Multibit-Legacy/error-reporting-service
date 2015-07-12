package org.multibit.hd.error_reporting.resources;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.jersey.api.client.Client;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.fest.assertions.api.Assertions.assertThat;

@Ignore
public class PublicErrorReportingResourceLoadTest {

  private static final Logger log = LoggerFactory.getLogger(PublicErrorReportingResourceLoadTest.class);

  /**
   * Entry point to the load tester
   *
   * @param args The command line arguments
   *
   * @throws Exception If something goes wrong
   */
  public static void main(String[] args) throws Exception {

    PublicErrorReportingResourceLoadTest loadTest = new PublicErrorReportingResourceLoadTest();

    loadTest.start();

  }

  private void start() {

    int MAX_EXECUTORS = 200;

    ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));

    for (int i = 0; i < MAX_EXECUTORS; i++) {

      ListenableFuture<String> matcherResponse = service.submit(new Callable<String>() {

        @Override
        public String call() throws Exception {

          return uploadErrorReport();

        }
      });

      Futures.addCallback(matcherResponse, new FutureCallback<String>() {

        public void onSuccess(String matcherResponse) {
          log.info("SUCCESS. Response: {}", matcherResponse);
        }

        public void onFailure(Throwable thrown) {
          log.error("FAILURE " + thrown.getMessage());
        }
      });

    }
  }

  /**
   * Create an error report and upload it
   *
   * @return The response from the error reporting service
   *
   */
  public synchronized String uploadErrorReport() throws Exception {

    // Load an example error report


    String payload = "";

    // Send the encrypted request to the Matcher
    log.info("Posting to server...");
    Client client = new Client();
    String actualResponse = client
      .resource("http://localhost:9191/error-reporting")
      .header("Content-Type", "text/plain")
      .accept(MediaType.TEXT_PLAIN_TYPE)
      .entity(payload)
      .post(String.class);
    log.info("Posted.");

    assertThat(actualResponse.length()).describedAs("Check for 204_NO_CONTENT may have address throttling active.").isGreaterThanOrEqualTo(20);

    // Build the encrypted Matcher response

    return "";

  }

}
