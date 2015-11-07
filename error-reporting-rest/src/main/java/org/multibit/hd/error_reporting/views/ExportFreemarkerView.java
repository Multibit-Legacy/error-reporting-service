package org.multibit.hd.error_reporting.views;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.yammer.dropwizard.views.View;
import org.multibit.hd.common.error_reporting.ErrorReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>View to provide the following to resources:</p>
 * <ul>
 * <li>Representation provided by a Freemarker template with a given model</li>
 * </ul>
 *
 * @since 0.4.0
 *
 */
public class ExportFreemarkerView extends View {

  private static final Logger log = LoggerFactory.getLogger(ExportFreemarkerView.class);

  private final ErrorReport model;
  private final String id;

  public ExportFreemarkerView(ErrorReport model, String id) {
    super("/views/ftl/export.ftl", Charsets.UTF_8);

    Preconditions.checkNotNull(model, "'model' must be present");

    this.model = model;
    this.id = id;

  }

  public ErrorReport getModel() {
    return model;
  }

  public String getId() {
    return id;
  }
}
