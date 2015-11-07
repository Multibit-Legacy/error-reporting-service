package org.multibit.hd.error_reporting.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.dropwizard.views.freemarker.FreemarkerViewRenderer;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.multibit.commons.utils.Dates;
import org.multibit.hd.common.error_reporting.ErrorReport;
import org.multibit.hd.common.error_reporting.ErrorReportLogEntry;
import org.multibit.hd.error_reporting.ErrorReportingService;
import org.multibit.hd.error_reporting.views.ExportFreemarkerView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>Task to provide the following to admin API:</p>
 * <ul>
 * <li>Trigger export of error reports as HTML packaged as a ZIP</li>
 * </ul>
 *
 * @since 0.4.0
 *  
 */
public class ExportHtmlTask extends Task {

  private static final Logger log = LoggerFactory.getLogger(ExportHtmlTask.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  private final Client elasticClient;

  public ExportHtmlTask() {
    super("export");

    elasticClient = ErrorReportingService.getElasticClient();

  }

  @Override
  public void execute(ImmutableMultimap<String, String> parameters, final PrintWriter output) throws Exception {

    boolean purge = parameters.containsKey("purge") && parameters.get("purge").iterator().next().equals("true");

    // Create a suitable output file
    File outputFile = new File("error-reports-" + Dates.formatBackupDate(Dates.nowUtc()) + ".zip");
    output.println("Extracting error reports from ELK as HTML into: '" + outputFile.getAbsolutePath() + "'...");
    output.flush();

    // Keep track of indices that have been visited (and can be purged)
    Set<String> visitedIndices = Sets.newHashSet();

    FreemarkerViewRenderer renderer = new FreemarkerViewRenderer();

    try (FileOutputStream fos = new FileOutputStream(outputFile);
         BufferedOutputStream bos = new BufferedOutputStream(fos);
         ZipOutputStream zos = new ZipOutputStream(bos)) {

      // Get all the current indices
      String[] allIndices = elasticClient
        .admin()
        .indices()
        .prepareGetIndex()
        .execute()
        .actionGet()
        .getIndices();

      int count = 0;

      for (String index : allIndices) {

        // Check for an error report index
        if (!index.startsWith("error-report-")) {
          continue;
        }

        // Get the ID
        String id = index.substring(index.length() - 7, index.length());

        output.print("Processing index '" + index + "' ");
        output.flush();

        // Avoid duplicating an index (we only need half of what is found)
        if (visitedIndices.contains("error-report-summary-" + id)
          || visitedIndices.contains("error-report-entries-" + id)) {
          output.println("Ignore - visited");
          output.flush();
          continue;
        }

        // Get the overall summary (1 per index)
        SearchResponse summary;
        try {
          summary = elasticClient
            .prepareSearch("error-report-summary-" + id)
            .setQuery(QueryBuilders.matchAllQuery())
            .setSize(10)
            .execute()
            .actionGet();
          // Mark as visited
          visitedIndices.add("error-report-summary-" + id);
        } catch (Exception e) {
          output.println("Ignore - no summary: " + e.getMessage());
          output.flush();
          continue;
        }

        // Get the individual entries (many per index limited to 200Kb)
        SearchResponse entries = null;
        try {
          entries = elasticClient
            .prepareSearch("error-report-entries-" + id)
            .setQuery(QueryBuilders.matchAllQuery())
            .setSize(5_000)
            .execute()
            .actionGet();
          // Mark as visited
          visitedIndices.add("error-report-entries-" + id);
        } catch (Exception e) {
          // Entries are held in the error report summary
        }

        ErrorReport errorReport = null;
        for (SearchHit summaryHit : summary.getHits()) {

          // Get the overall report
          errorReport = mapper.readValue(summaryHit.sourceAsString(), ErrorReport.class);

          if (entries != null) {
            // Fill in the entries separately
            for (SearchHit entryHit : entries.getHits()) {

              ErrorReportLogEntry errorReportLogEntry = mapper.readValue(entryHit.sourceAsString(), ErrorReportLogEntry.class);
              errorReport.getLogEntries().add(errorReportLogEntry);

            }
          }

        }

        // Build a suitable HTML page
        ExportFreemarkerView view = new ExportFreemarkerView(errorReport, id);
        ByteArrayOutputStream reportBaos = new ByteArrayOutputStream();
        renderer.render(view, Locale.UK, reportBaos);

        // Add to overall ZIP
        zos.putNextEntry(new ZipEntry("error-report-" + id + ".html"));
        zos.write(reportBaos.toByteArray());
        zos.closeEntry();

        output.println("OK");
        output.flush();

        // Keep track of the overall number processed
        count++;

        if (count > 15) {
          output.println("Stopping early to avoid overloading the node.");
          output.flush();
          break;
        }

      }

      // Check for a purge
      if (purge && !visitedIndices.isEmpty()) {

          // Delete the index
          try {
            output.print("Purging visited indices...");
            output.flush();
            ListenableActionFuture<DeleteIndexResponse> future = elasticClient
              .admin()
              .indices()
              .prepareDelete(visitedIndices.toArray(new String[visitedIndices.size()]))
              .setTimeout(TimeValue.timeValueSeconds(1))
              .execute();
            future.addListener(new ActionListener<DeleteIndexResponse>() {
              @Override
              public void onResponse(DeleteIndexResponse deleteIndexResponse) {
                output.println("OK");
                output.flush();
              }

              @Override
              public void onFailure(Throwable e) {
                output.println("FAILED - "+e.getMessage());
                output.flush();
              }
            });

            // Allow time for the index delete to finish in the background
            Thread.sleep(3500);

          } catch (Exception e) {
            output.println("FAILED - "+e.getMessage());
            output.flush();
          }

      }

      output.println("\n\nDone. Use 'scp -P <port> <user>@<host>:" + outputFile.getAbsolutePath() + " .' to retrieve the archive.");
      output.flush();

    } catch (Exception e) {
      output.println("ERROR - " + e.getMessage());
      output.flush();
    }

  }
}
