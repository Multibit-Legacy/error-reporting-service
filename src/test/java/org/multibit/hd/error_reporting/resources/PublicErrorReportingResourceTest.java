package org.multibit.hd.error_reporting.resources;

import com.google.common.io.ByteStreams;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;
import org.multibit.hd.error_reporting.testing.FixtureAsserts;
import org.multibit.hd.error_reporting.utils.StreamUtils;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class PublicErrorReportingResourceTest extends ResourceTest {

  public static final String TEST_MATCHER_PUBLIC_KEYRING_FILE = "/src/test/resources/fixtures/gpg/pubring.gpg";

  public static final String TEST_MATCHER_SECRET_KEYRING_FILE = "/src/test/resources/fixtures/gpg/secring.gpg";

  public static final String TEST_PUBLIC_KEY_FILE = "/src/test/resources/fixtures/gpg/public-key.asc";

  /**
   * The password used in the generation of the test PGP keys
   */
  public static final char[] TEST_DATA_PASSWORD = "password".toCharArray();

  private PublicErrorReportingResource testObject;

  @Override
  protected void setUpResources() throws Exception {

    InputStream publicKeyStream = PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/gpg/public-key.asc");
    InputStream secringStream = PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/gpg/secring.gpg");

    String publicKey = StreamUtils.toString(publicKeyStream);
    byte[] secring = ByteStreams.toByteArray(secringStream);

    testObject = new PublicErrorReportingResource(
      secring,
      "password".toCharArray(),
      publicKey
    );

    // Configure resources
    addResource(testObject);

  }

  @Test
  public void GET_MatcherPublicKey() throws Exception {

    // Build the request
    String actualResponse = client()
      .resource("/error-reporting/public-key")
      .header("Content-Type", "text/plain")
      .accept(MediaType.TEXT_PLAIN_TYPE)
      .get(String.class);

    FixtureAsserts.assertStringMatchesStringFixture(
      "Get service public key",
      actualResponse,
      "/fixtures/gpg/public-key.asc"
    );

  }

  @Test
  public void POST_EncryptedPayerRequest_String() throws Exception {

    String payload = StreamUtils.toString(PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/error_reporting/test-error-report.txt.asc"));

    // Send the encrypted request to the service
    String actualResponse = client()
      .resource("/error-reporting")
      .header("Content-Type", "text/plain")
      .accept(MediaType.TEXT_PLAIN_TYPE)
      .entity(payload)
      .post(String.class);

    assertThat(actualResponse).isEqualTo("OK_UNKNOWN");

  }

}
