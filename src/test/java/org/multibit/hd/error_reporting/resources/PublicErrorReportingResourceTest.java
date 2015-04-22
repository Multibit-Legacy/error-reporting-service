package org.multibit.hd.error_reporting.resources;

import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;
import org.multibit.hd.error_reporting.testing.FixtureAsserts;
import org.multibit.hd.error_reporting.utils.StreamUtils;

import javax.ws.rs.core.MediaType;
import java.io.InputStream;

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

    InputStream is = PublicErrorReportingResourceTest.class.getResourceAsStream("/fixtures/gpg/public-key.asc");

    String publicKey = StreamUtils.toString(is);

    testObject = new PublicErrorReportingResource(publicKey);

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

//  @Test
//  public void POST_EncryptedPayerRequest_String() throws Exception {
//
//    // Create a payer
//    Payer payer = newTestUploader();
//
//    BRITWalletId britWalletId = newBritWalletId();
//
//    // Create a random session id
//    byte[] sessionId = newSessionId();
//
//    // Create a first transaction date (in real life this would come from a wallet)
//    Optional<Date> firstTransactionDateOptional = Optional.of(new Date());
//
//    // Ask the payer to create an EncryptedPayerRequest containing a BRITWalletId, a session id and a firstTransactionDate
//    PayerRequest payerRequest = payer.newPayerRequest(britWalletId, sessionId, firstTransactionDateOptional);
//    assertThat(payerRequest).isNotNull();
//    // Encrypt the PayerRequest with the Matcher PGP public key.
//    EncryptedPayerRequest encryptedPayerRequest = payer.encryptPayerRequest(payerRequest);
//
//    String payload = new String(encryptedPayerRequest.getPayload(), Charsets.UTF_8);
//
//    // Send the encrypted request to the Matcher
//    byte[] actualResponse = client()
//      .resource("/error_reporting")
//      .header("Content-Type", "text/plain")
//      .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
//      .entity(payload)
//      .post(byte[].class);
//
//    assertThat(actualResponse.length).isGreaterThanOrEqualTo(20);
//
//    // Build the encrypted Matcher response
//    EncryptedMatcherResponse encryptedMatcherResponse = new EncryptedMatcherResponse(actualResponse);
//
//    // Payer can decrypt the encryptedMatcherResponse because it knows the BRITWalletId and session id
//    MatcherResponse plainMatcherResponse = payer.decryptMatcherResponse(encryptedMatcherResponse);
//    assertThat(plainMatcherResponse).isNotNull();
//
//    // Get the list of addresses the Payer will use
//    Set<String> bitcoinAddresses = plainMatcherResponse.getBitcoinAddresses();
//    assertThat(bitcoinAddresses).isNotNull();
//
//    // Get the replay date for the wallet
//    Date replayDate = plainMatcherResponse.getReplayDate().get();
//    assertThat(replayDate).isNotNull();
//
//  }
//
//  /**
//   * @return A test uploader
//   *
//   * @throws Exception If something goes wrong
//   */
//  private Payer newTestUploader() throws Exception {
//
//    // Load the example Matcher PGP public key
//    InputStream matcherPublicKeyInputStream = PublicErrorReportingResource.class.getResourceAsStream("/error_reporting/matcher-pubkey.asc");
//    PGPPublicKey matcherPGPPublicKey = PGPUtils.readPublicKey(matcherPublicKeyInputStream);
//
//    log.info("Matcher public key id = " + matcherPGPPublicKey.getKeyID());
//
//    PayerConfig payerConfig = new PayerConfig(matcherPGPPublicKey);
//
//    // Create and verify the Payer
//    Payer payer = Payers.newBasicPayer(payerConfig);
//    assertThat(payer).isNotNull();
//    assertThat(payer.getConfig().getMatcherPublicKey()).isEqualTo(matcherPGPPublicKey);
//
//    return payer;
//  }
//
//  /**
//   * @return A test Matcher
//   *
//   * @throws Exception If something goes wrong
//   */
//  private Matcher createTestMatcher() throws Exception {
//
//    // Find the example Matcher PGP secret key ring file
//    File matcherSecretKeyFile = FixtureUtils.makeFile("", TEST_MATCHER_SECRET_KEYRING_FILE);
//    MatcherConfig matcherConfig = new MatcherConfig(matcherSecretKeyFile, TEST_DATA_PASSWORD);
//
//    // Create a random temporary directory for the Matcher store to use
//    File matcherStoreDirectory = FileUtils.makeRandomTemporaryDirectory();
//    MatcherStore matcherStore = MatcherStores.newBasicMatcherStore(matcherStoreDirectory);
//
//    Matcher matcher = Matchers.newBasicMatcher(matcherConfig, matcherStore);
//    assertThat(matcher).isNotNull();
//
//    // Add some test data for today's bitcoin addresses
//    Set<String> bitcoinAddresses = Sets.newHashSet();
//    bitcoinAddresses.add("cat");
//    bitcoinAddresses.add("dog");
//    bitcoinAddresses.add("elephant");
//    bitcoinAddresses.add("worm");
//
//    matcherStore.storeBitcoinAddressesForDate(bitcoinAddresses, new Date());
//
//    return matcher;
//  }

}
