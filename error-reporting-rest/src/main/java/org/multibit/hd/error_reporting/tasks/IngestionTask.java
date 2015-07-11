package org.multibit.hd.error_reporting.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;
import org.multibit.hd.error_reporting.ErrorReportingService;
import org.multibit.hd.error_reporting.resources.PublicErrorReportingResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * <p>Task to provide the following to admin API:</p>
 * <ul>
 * <li>Trigger ingestion of persisted error reports</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class IngestionTask extends Task {

  private static final Logger log = LoggerFactory.getLogger(IngestionTask.class);

  public IngestionTask() {
    super("ingest");
  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, final PrintWriter output) throws Exception {

    // Must be new or uncached to be here
    log.debug("Ingesting persisted error reports to ELK.");

    final File errorReportsDirectory = new File(ErrorReportingService.getErrorReportingDirectory().getAbsolutePath() + "/error-reports");
    if (!errorReportsDirectory.exists()) {
      output.println("Nothing to do. '" + errorReportsDirectory + "' is not present.");
      return;
    }

    Path errorReportsPath = errorReportsDirectory.toPath();
    FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

        output.print("Processing '" + file.toString() + "' ");

        // Read file
        String encryptedPayload = new String(Files.readAllBytes(file));

        // Create a new resource (simulate an individual request)
        PublicErrorReportingResource resource = new PublicErrorReportingResource();

        final Response response = resource.submitEncryptedErrorReport(encryptedPayload);
        if (response.getStatus() == 201) {
          output.println("OK");
        } else {
          output.println("FAIL: " + response.getStatus());
        }

        Files.delete(file);

        return FileVisitResult.CONTINUE;
      }
    };

    try {
      Files.walkFileTree(errorReportsPath, visitor);
    } catch (IOException e) {
      log.error("Failed to ingest error reports", e);
      output.println("FAIL: " + e.getMessage());
    }

  }
}
