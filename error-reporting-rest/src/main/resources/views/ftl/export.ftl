<#-- @ftlvariable name="" type="org.multibit.hd.error_reporting.views.ExportFreemarkerView" -->
<#-- Template for the exported error reports from the admin console -->

<#-- Required for IE to render correctly -->
<!DOCTYPE HTML>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta http-equiv="Content-type" content="text/html; charset=UTF-8">

  <title>Error Report ${id?html}</title>

  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="author" content="Bitcoin Solutions Ltd" />

</head>
<body>
<style>
  .TRACE {
    color: lightgrey;
  }
  .DEBUG {
    color: grey;
  }
  .INFO {
    color: black;
  }
  .WARN {
    color: orange;
  }
  .ERROR {
    color: red;
  }
</style>
<h2>Error Report ${id?html}</h2>
<h3>Summary</h3>
<p>User notes:<br/>${model.userNotes?html}</p>
<ul>
  <li>App version: ${model.appVersion?html}</li>
  <li>OS details: ${model.osName?html}&nbsp;${model.osVersion?html}&nbsp;${model.osArch?html}</li>
</ul>
<h3>Log entries</h3>
<pre>
<#list model.logEntries as logEntry>
<span class="${logEntry.level}">[${logEntry.timestamp?html}] ${logEntry.level?html} ${logEntry.threadName?html} ${logEntry.loggerName?html} - ${logEntry
.message?html}</span>
<#if logEntry.stackTrace??>
<br/>${logEntry.stackTrace?html}<br/>
</#if>
</#list>
</pre>
</body>

</html>