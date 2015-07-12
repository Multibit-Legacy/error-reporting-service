package org.multibit.hd.error_reporting.resources;

import com.google.common.io.ByteStreams;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;
import org.multibit.hd.common.error_reporting.ErrorReportResult;
import org.multibit.hd.common.error_reporting.ErrorReportStatus;
import org.multibit.hd.error_reporting.testing.FixtureAsserts;
import org.multibit.hd.error_reporting.utils.StreamUtils;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class PublicErrorReportingResourceTest extends ResourceTest {

  @Override
  protected void setUpResources() throws Exception {

    InputStream publicKeyStream = PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/gpg/public-key.asc");
    InputStream secringStream = PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/gpg/secring.gpg");

    String publicKey = StreamUtils.toString(publicKeyStream);
    byte[] secring = ByteStreams.toByteArray(secringStream);

    PublicErrorReportingResource testObject = new PublicErrorReportingResource(
      secring,
      "password".toCharArray(),
      publicKey,
      null
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

    String payload = StreamUtils.toString(PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/error_reporting/error-report.json.asc"));

    // Send the encrypted request to the service
    ErrorReportResult actualResponse = client()
      .resource("/error-reporting")
      .header("Content-Type", "text/plain")
      .accept(MediaType.APPLICATION_JSON_TYPE)
      .entity(payload)
      .post(ErrorReportResult.class);

    assertThat(actualResponse.getErrorReportStatus()).isEqualTo(ErrorReportStatus.UPLOAD_OK_UNKNOWN);

  }

}
