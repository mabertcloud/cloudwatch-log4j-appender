// Planon Enterprise Edition Source file: CloudJavaUtilLoggingHandler.java
// Copyright Planon 1997-2016. All Rights Reserved.
package nl.planon.cloud.aws.logging.log4jappenders;

import java.util.logging.*;

/**
 * CloudJavaUtilLoggingHandler
 */
public class CloudJavaUtilLoggingHandler extends Handler
{
  //~ Instance Variables ---------------------------------------------------------------------------

  private boolean doneHeader;
  private CloudWatchReporter reporter;

  //~ Constructors ---------------------------------------------------------------------------------

  /**
   * Creates a new CloudJavaUtilLoggingHandler object.
   */
  public CloudJavaUtilLoggingHandler()
  {
    configure();
    System.err.println("Created logger!");
  }

  //~ Methods --------------------------------------------------------------------------------------

  /**
   * DOCUMENT ME!
   *
   * @throws SecurityException DOCUMENT ME!
   */
  @Override public synchronized void close() throws SecurityException
  {
    if (this.reporter != null)
    {
      this.reporter.stop();
      this.reporter = null;
    }
  }


  /**
   * DOCUMENT ME!
   */
  @Override public void flush()
  {
  }


  /**
   * DOCUMENT ME!
   *
   * @param aMessage aArg0 DOCUMENT ME!
   */
  @Override public void publish(LogRecord aMessage)
  {
    if (!isLoggable(aMessage) || (this.reporter == null))
    {
      return;
    }
    String msg;
    try
    {
      msg = getFormatter().format(aMessage);
    }
    catch (Exception ex)
    {
      // We don't want to throw an exception here, but we
      // report the exception to any registered ErrorManager.
      reportError(null, ex, ErrorManager.FORMAT_FAILURE);
      return;
    }

    try
    {
      if (!this.doneHeader)
      {
        String head = getFormatter().getHead(this);
        System.err.print(head);
        if (!isNullOrEmpty(head))
        {
          this.reporter.append(head, aMessage.getMillis());
        }
        this.doneHeader = true;
      }
      System.err.print(msg);
      if (!isNullOrEmpty(msg))
      {
        this.reporter.append(msg, aMessage.getMillis());
      }
    }
    catch (Exception ex)
    {
      // We don't want to throw an exception here, but we
      // report the exception to any registered ErrorManager.
      reportError(null, ex, ErrorManager.WRITE_FAILURE);
    }
  }


  /**
   * DOCUMENT ME!
   */
  private void configure()
  {
    if (this.reporter != null)
    {
      return;
    }
    LogManager manager = LogManager.getLogManager();
    String cname = getClass().getName();
    setLevel(LogManagerHelper.getLevelProperty(manager, cname + ".level", Level.INFO));
    setFilter(LogManagerHelper.getFilterProperty(manager, cname + ".filter", null));
    setFormatter(LogManagerHelper.getFormatterProperty(manager, cname + ".formatter", new SimpleFormatter()));
    setLevel(Level.INFO);
    setFilter(null);
    setFormatter(new SimpleFormatter());
    try
    {
      setEncoding(LogManagerHelper.getStringProperty(manager, cname + ".encoding", null));
    }
    catch (Exception ex)
    {
      try
      {
        setEncoding(null);
      }
      catch (Exception ex2)
      {
        // doing a setEncoding with null should always work.
        // assert false;
      }
    }
    System.err.println("Loading AWS log settings...");

    String logGroup = LogManagerHelper.getStringProperty(manager, cname + ".awsLogGroupName", "LocalLogs");
    String logStream = LogManagerHelper.getStringProperty(manager, cname + ".awsLogStreamName", "local");
    String flushTime = LogManagerHelper.getStringProperty(manager, cname + ".awsLogStreamFlushPeriodInSeconds", "5");
    String regionName = LogManagerHelper.getStringProperty(manager, cname + ".awsRegionName", "eu-central-1");
    System.err.println("Loaded AWS log settings: lg: [" + logGroup + "] ls: [" + logStream + "] ft: [" + flushTime + "] rg: [" + regionName + "]");
    this.reporter = new CloudWatchReporter(logGroup, logStream, flushTime, regionName);
    this.reporter.start();
  }


  /**
   * DOCUMENT ME!
   *
   * @param  aText DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  private boolean isNullOrEmpty(String aText)
  {
    return ((aText == null) || (aText.length() == 0));
  }
}
