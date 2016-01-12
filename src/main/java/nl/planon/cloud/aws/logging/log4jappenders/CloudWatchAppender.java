//// ButAds Source file: CloudWatchAppender.java
//// Copyright ButAds 2011-2016. All Rights Reserved.
//package nl.planon.cloud.aws.logging.log4jappenders;
//
//import com.amazonaws.services.logs.model.*;
//
//import static nl.planon.cloud.aws.logging.log4jappenders.Config.*;
//
//import org.apache.logging.log4j.core.*;
//import org.apache.logging.log4j.core.appender.*;
//import org.apache.logging.log4j.core.config.plugins.*;
//import org.apache.logging.log4j.core.layout.*;
//
//import java.io.*;
//
///**
// * Created by mihailo.despotovic on 4/8/15.
// */
//@Plugin(name = "CloudWatchAppender", category = "Core", elementType = "appender", printObject = true)
//public class CloudWatchAppender extends AbstractAppender
//{
//  //~ Instance Variables ---------------------------------------------------------------------------
//
//  private final CloudWatchReporter reporter;
//
//  //~ Constructors ---------------------------------------------------------------------------------
//
//  /**
//   * Creates a new CloudWatchAppender object.
//   *
//   * @param name                             DOCUMENT ME
//   * @param awsLogGroupName                  DOCUMENT ME
//   * @param awsLogStreamName                 DOCUMENT ME
//   * @param awsLogStreamFlushPeriodInSeconds DOCUMENT ME
//   * @param awsRegion                        DOCUMENT ME
//   * @param layout                           DOCUMENT ME
//   */
//  private CloudWatchAppender(final String name, final String awsLogGroupName, final String awsLogStreamName, final String awsLogStreamFlushPeriodInSeconds, final String awsRegion, final Layout<Serializable> layout)
//  {
//    super(name, null, layout == null ? PatternLayout.createDefaultLayout() : layout, false);
//    this.reporter = new CloudWatchReporter(awsLogGroupName, awsLogStreamName, awsLogStreamFlushPeriodInSeconds, awsRegion);
//  }
//
//  //~ Methods --------------------------------------------------------------------------------------
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param  name                             DOCUMENT ME
//   * @param  awsLogGroupName                  DOCUMENT ME
//   * @param  awsLogStreamName                 DOCUMENT ME
//   * @param  awsLogStreamFlushPeriodInSeconds DOCUMENT ME
//   * @param  awsLogRegion                     DOCUMENT ME
//   * @param  layout                           DOCUMENT ME
//   *
//   * @return DOCUMENT ME
//   */
//  @PluginFactory public static CloudWatchAppender createAppender(@PluginAttribute("name") String name,
//    @PluginAttribute("awsLogGroupName") String awsLogGroupName,
//    @PluginAttribute("awsLogStreamName") String awsLogStreamName,
//    @PluginAttribute("awsLogStreamFlushPeriodInSeconds") String awsLogStreamFlushPeriodInSeconds,
//    @PluginAttribute("awsLogRegion") String awsLogRegion,
//    @PluginElement("Layout") Layout<Serializable> layout)
//  {
//    System.out.println("Starting Cloudwatch appender");
//    return new CloudWatchAppender(name == null ? DEFAULT_LOG_APPENDER_NAME : name, awsLogGroupName == null ? DEFAULT_AWS_LOG_GROUP_NAME : awsLogGroupName, awsLogStreamName, awsLogStreamFlushPeriodInSeconds, awsLogRegion, layout);
//  }
//
//
//  /**
//   * Create AWS log event based on the log4j log event and add it to the queue.
//   *
//   * @param event DOCUMENT ME
//   */
//  @Override public void append(final LogEvent event)
//  {
//    final InputLogEvent awsLogEvent = new InputLogEvent();
//    final long timestamp = event.getTimeMillis();
//    final String message = new String(getLayout().toByteArray(event));
//    this.reporter.append(message, timestamp);
//  }
//
//  // tiny helper self-describing methods
//
//
//  /**
//   * DOCUMENT ME
//   */
//  @Override public void start()
//  {
//    super.start();
//    this.reporter.start();
//  }
//
//
//  /**
//   * DOCUMENT ME
//   */
//  @Override public void stop()
//  {
//    super.stop();
//    this.reporter.stop();
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param s DOCUMENT ME
//   */
//  private void debug(final String s)
//  {
//    System.err.println(" CloudWatchAppender: " + s);
//  }
//}
