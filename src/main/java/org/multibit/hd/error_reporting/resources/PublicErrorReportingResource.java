package org.multibit.hd.error_reporting.resources;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.metrics.annotation.Timed;
import org.multibit.hd.error_reporting.caches.ErrorReportingResponseCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

  private final MessageDigest sha1Digest;

  private final String servicePublicKey;

  /**
   */
  public PublicErrorReportingResource(String servicePublicKey) throws NoSuchAlgorithmException, IOException {

    this.servicePublicKey = servicePublicKey;
    this.sha1Digest = MessageDigest.getInstance("SHA1");

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
  public Response getPublicKey() throws IOException {

    return Response
      .ok(servicePublicKey)
      .build();

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
  public Response submitEncryptedErrorReport(String payload) throws Exception {

    Preconditions.checkNotNull(payload, "'payload' must be present");
    Preconditions.checkState(payload.length() < MAX_PAYLOAD_LENGTH, "'payload' is too long");

    String result = processEncryptedErrorReport(payload.getBytes(Charsets.UTF_8));

    return Response
      .created(UriBuilder.fromPath("/error-reporting").build())
      .entity(result)
      .build();

  }

  private String processEncryptedErrorReport(byte[] payload) {

    // Check the cache
    byte[] sha1 = sha1Digest.digest(payload);

    Optional<String> cachedResponse = ErrorReportingResponseCache.INSTANCE.getByErrorReportDigest(sha1);

    if (cachedResponse.isPresent()) {
      log.debug("Using cached response");
      return cachedResponse.get();
    }

    // Must be new or uncached to be here
    log.debug("Creating response");

    return "";

//    try {
//
//      // TODO Decrypt the response using the secring
//
//    } catch (Exception e) {
//      log.error(e.getMessage(), e);
//      throw new WebApplicationException(Response.Status.BAD_REQUEST);
//    }
//
//    return response;

  }

}
