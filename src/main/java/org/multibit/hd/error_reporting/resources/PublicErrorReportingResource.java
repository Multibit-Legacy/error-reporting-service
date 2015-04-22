package org.multibit.hd.error_reporting.resources;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.metrics.annotation.Timed;
import org.multibit.hd.brit.crypto.PGPUtils;
import org.multibit.hd.error_reporting.ErrorReportingService;
import org.multibit.hd.error_reporting.caches.ErrorReportingResponseCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * <p>Resource to provide the following to application:</p>
 * <ul>
 * <li>Provision of error reporting responses</li>
 * <li>Handles decrypting error reports and handing them upstream</li>
 * </ul>
 *
 * @since 0.0.1
 */
@Path("/error-reporting")
public class PublicErrorReportingResource extends BaseResource {

  private static final Logger log = LoggerFactory.getLogger(PublicErrorReportingResource.class);

  /**
   * The maximum length of the payload (typical value is 680 bytes)
   */
  private final static int MAX_PAYLOAD_LENGTH = 2_000_000;

  private final char[] password;
  private final byte[] secring;
  private final String servicePublicKey;

  /**
   * Default constructor used by Jersey and reads from the ErrorReportingService
   */
  public PublicErrorReportingResource() {
    this(
      ErrorReportingService.getSecring(),
      ErrorReportingService.getPassword(),
      ErrorReportingService.getServicePublicKey()
    );
  }

  /**
   * Full constructor used by resource tests
   */
  public PublicErrorReportingResource(byte[] secring, char[] password, String servicePublicKey) {

    this.secring = Arrays.copyOf(secring, secring.length);
    this.password = Arrays.copyOf(password, password.length);
    this.servicePublicKey = servicePublicKey;

  }

  /**
   * Allow an uploader to compare or obtain the error reporting public key
   *
   * @return A localised view containing plain text
   */
  @GET
  @Path("/public-key")
  @Consumes("text/plain")
  @Produces("text/plain")
  @Timed
  @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
  public Response getPublicKey() {
    return Response.ok(servicePublicKey).build();
  }

  /**
   * Allow a client to upload an error report as an ASCII armored payload (useful for REST clients)
   *
   * @param payload The encrypted error report payload
   *
   * @return A plain text response
   */
  @POST
  @Consumes("text/plain")
  @Produces("text/plain")
  @Timed
  @CacheControl(noCache = true)
  public Response submitEncryptedErrorReport(String payload) {

    Preconditions.checkNotNull(payload, "'payload' must be present");
    Preconditions.checkState(payload.length() < MAX_PAYLOAD_LENGTH, "'payload' is too long");

    String result = processEncryptedErrorReport(payload.getBytes(Charsets.UTF_8));

    return Response
      .created(UriBuilder.fromPath("/error-reporting").build())
      .entity(result)
      .build();

  }

  private String processEncryptedErrorReport(byte[] payload) {

    byte[] sha1 = ErrorReportingService.digest(payload);
    Optional<String> cachedResponse = ErrorReportingResponseCache.INSTANCE.getByErrorReportDigest(sha1);

    if (cachedResponse.isPresent()) {
      log.debug("Using cached response");
      return cachedResponse.get();
    }

    // Must be new or uncached to be here
    log.debug("Creating response");

    // Decrypt the payload
    ByteArrayInputStream encryptedBais = new ByteArrayInputStream(payload);
    ByteArrayOutputStream plainBaos = new ByteArrayOutputStream();
    try {
      PGPUtils.decryptFile(encryptedBais, plainBaos, new ByteArrayInputStream(secring), password);
    } catch (Exception e) {
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    }

    // Push to ELK and cache the result
    String result = pushToElk(plainBaos);
    ErrorReportingResponseCache.INSTANCE.put(sha1, result);
    return result;

  }

  /**
   * @param payload The plaintext payload
   *
   * @return The result of the push (e.g. "OK_UNKNOWN")
   */
  private String pushToElk(ByteArrayOutputStream payload) {

    System.out.printf("Payload as String:%n%s%n", new String(payload.toByteArray(), Charsets.UTF_8));

    return "OK_UNKNOWN";
  }

}
