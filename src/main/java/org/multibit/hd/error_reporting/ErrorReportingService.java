package org.multibit.hd.error_reporting;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.eclipse.jetty.server.session.SessionHandler;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.multibit.hd.brit.crypto.PGPUtils;
import org.multibit.hd.error_reporting.elastic_search.TransportClientFactory;
import org.multibit.hd.error_reporting.health.CryptoFilesHealthCheck;
import org.multibit.hd.error_reporting.health.ESHealthCheck;
import org.multibit.hd.error_reporting.resources.PublicErrorReportingResource;
import org.multibit.hd.error_reporting.resources.RuntimeExceptionMapper;
import org.multibit.hd.error_reporting.servlets.SafeLocaleFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

/**
 * <p>Service to provide the following to application:</p>
 * <ul>
 * <li>Provision of access to resources</li>
 * </ul>
 * <p>Use <code>java -jar error-reporting-service-develop-SNAPSHOT.jar server config.yml</code> to start</p>
 *
 * @since 0.0.1
 *  
 */
public class ErrorReportingService extends Service<ErrorReportingConfiguration> {

  private static final Logger log = LoggerFactory.getLogger(ErrorReportingService.class);

  /**
   * The error reporting support directory
   */
  private static final String ERROR_REPORTING_DIRECTORY = "/var/error-reporting";

  /**
   * The service public key
   */
  private static String servicePublicKey;

  /**
   * The PGP secring file
   */
  private static byte[] secring;

  /**
   * PGP secring password
   */
  private static char[] password;

  /**
   * The Elasticsearch client
   */
  private static TransportClient elasticClient;

  /**
   * Main entry point to the application
   *
   * @param args CLI arguments
   *
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    // Start the logging factory
    LoggingFactory.bootstrap();

    // Securely read the password from the console
    password = readPassword();

    System.out.print("Crypto files ");
    // PGP decrypt the file (requires the private key ring that is password protected)
    System.out.println("OK");

    System.out.print("Crypto keys ");
    try {
      verifyCryptoFiles(); // Outputs OK on success
    } catch (PGPException e) {
      System.err.println("FAIL (" + e.getMessage() + "). Checksum means password is incorrect.");
      System.exit(-1);
    }
    System.out.print("Elasticsearch ");
    try {
      verifyElasticsearch();
      if (elasticClient == null) {
        throw new IllegalStateException("Elasticsearch client is not present");
      }
    } catch (IOException e) {
      System.err.println("FAIL (" + e.getMessage() + ").");
      System.exit(-1);
    }

    // Create the service
    System.out.println("OK\nStarting service...\n");

    // Load the public key
    servicePublicKey = Files.toString(getPublicKeyFile(getErrorReportingDirectory()), Charsets.UTF_8);

    // Must be OK to be here
    new ErrorReportingService().run(args);

  }

  /**
   * Verify the Elasticsearch environment is set up correctly
   */
  private static void verifyElasticsearch() throws IOException {

    // TODO Consider adding configuration to YAML
    Map<String,String> settings = Maps.newHashMap();
    settings.put("cluster.name", "elasticsearch_brew");

    elasticClient = TransportClientFactory.newClient(
      settings,
      "localhost",
      9300,
      false
    );

  }

  /**
   * Verify the crypto environment is set up correctly
   *
   * @throws Exception If something goes wrong
   */
  public static void verifyCryptoFiles() throws Exception {

    final File errorReportingDirectory = getErrorReportingDirectory();
    final File secretKeyringFile = getSecretKeyringFile(errorReportingDirectory);
    final File testCryptoFile = getTestCryptoFile(errorReportingDirectory);

    // Attempt to encrypt the test file
    ByteArrayOutputStream armoredOut = new ByteArrayOutputStream(1024);
    PGPPublicKey publicKey = PGPUtils.readPublicKey(new FileInputStream(getPublicKeyFile(errorReportingDirectory)));
    PGPUtils.encryptFile(armoredOut, testCryptoFile, publicKey);

    // Attempt to decrypt the test file
    ByteArrayInputStream armoredIn = new ByteArrayInputStream(armoredOut.toByteArray());
    ByteArrayOutputStream decryptedOut = new ByteArrayOutputStream(1024);
    secring = ByteStreams.toByteArray(new FileInputStream(secretKeyringFile));
    PGPUtils.decryptFile(armoredIn, decryptedOut, new ByteArrayInputStream(getSecring()), password);

    // Verify that the decryption was successful
    String testCrypto = decryptedOut.toString();
    System.out.println(testCrypto);

    if (!"OK".equals(testCrypto)) {
      throw new PGPException("Incorrect message in test crypto file");
    }
  }

  /**
   * @return The public key to serve to consumers of this service
   */
  public static String getServicePublicKey() {
    return servicePublicKey;
  }

  /**
   * @param payload The payload
   *
   * @return A SHA1 digest of the payload
   */
  public static byte[] digest(byte[] payload) {

    final MessageDigest sha1Digest;
    try {
      sha1Digest = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException e) {
      log.error("SHA1 not supported on this machine.");
      return null;
    }

    return sha1Digest.digest(payload);

  }

  /**
   * @return A copy of the PGP secring
   */
  public static byte[] getSecring() {
    return Arrays.copyOf(secring, secring.length);
  }

  /**
   * @return A copy of the PGP secring password
   */
  public static char[] getPassword() {
    return Arrays.copyOf(password, password.length);
  }

  /**
   * @return The Elasticsearch client
   */
  public static Client getElasticClient() {
    return elasticClient;
  }

  /**
   * @return The password taken from the console (or "password" if running in an IDE)
   */
  private static char[] readPassword() {

    Console console = System.console();
    final char[] password;
    if (console == null) {
      System.out.println("Could not obtain a console. Assuming an IDE and test data.");
      password = "password".toCharArray();
    } else {
      password = console.readPassword("%s", "Enter password:");
      if (password == null) {
        System.err.println("Could not read the password.");
        System.exit(-1);
      }
      System.out.println("Working...");
    }

    return password;

  }

  /**
   * @return The error reporting support directory
   */
  private static File getErrorReportingDirectory() {

    final File errorReportingDirectory = new File(ERROR_REPORTING_DIRECTORY);
    if (!errorReportingDirectory.exists()) {
      System.err.printf(
        "Error reporting directory not present at '%s'.%nConsider copying the example structure from src/test/resources%n",
        errorReportingDirectory.getAbsolutePath());
      System.exit(-1);
    }

    return errorReportingDirectory;
  }

  private static File getSecretKeyringFile(File errorReportingDirectory) {

    File secretKeyringFile = new File(errorReportingDirectory, "gpg/secring.gpg");
    if (!secretKeyringFile.exists()) {
      System.err.printf("Error reporting secret keyring not present at '%s'.%n", secretKeyringFile.getAbsolutePath());
      System.exit(-1);
    }

    return secretKeyringFile;
  }

  private static File getPublicKeyFile(File errorReportingDirectory) {

    File matcherPublicKeyFile = new File(errorReportingDirectory, "gpg/public-key.asc");
    if (!matcherPublicKeyFile.exists()) {
      System.err.printf("Public key not present at '%s'.%n", matcherPublicKeyFile.getAbsolutePath());
      System.exit(-1);
    }

    return matcherPublicKeyFile;
  }

  private static File getTestCryptoFile(File errorReportingDirectory) throws IOException {

    File testCryptoFile = new File(errorReportingDirectory, "gpg/test.txt");
    if (!testCryptoFile.exists()) {
      if (!testCryptoFile.createNewFile()) {
        System.err.printf("Could not create crypto test file: '%s'.%n", testCryptoFile.getAbsolutePath());
        System.exit(-1);
      }
      // Populate it with a simple test
      Writer writer = new FileWriter(testCryptoFile);
      writer.write("OK");
      writer.flush();
      writer.close();

    }

    return testCryptoFile;
  }

  @Override
  public void initialize(Bootstrap<ErrorReportingConfiguration> bootstrap) {

    // Do nothing

  }

  @Override
  public void run(ErrorReportingConfiguration errorReportingConfiguration, Environment environment) throws Exception {

    log.info("Scanning environment...");

    // Configure environment
    environment.addResource(PublicErrorReportingResource.class);

    // Health checks
    environment.addHealthCheck(new CryptoFilesHealthCheck());
    environment.addHealthCheck(new ESHealthCheck("Elasticsearch", getElasticClient()));

    // Providers
    environment.addProvider(new ViewMessageBodyWriter());
    environment.addProvider(new RuntimeExceptionMapper());

    // Filters
    environment.addFilter(new SafeLocaleFilter(), "/*");

    // Session handler
    environment.setSessionHandler(new SessionHandler());

  }
}
