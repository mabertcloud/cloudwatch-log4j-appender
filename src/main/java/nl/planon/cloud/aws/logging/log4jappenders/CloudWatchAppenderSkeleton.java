//// Planon Enterprise Edition Source file: CloudWatchAppenderSkeleton.java
//// Copyright Planon 1997-2016. All Rights Reserved.
//package nl.planon.cloud.aws.logging.log4jappenders;
//
//import org.apache.log4j.*;
//import org.apache.log4j.spi.*;
//
///**
// * CloudWatchAppenderSkeleton
// */
//public class CloudWatchAppenderSkeleton extends AppenderSkeleton
//{
//  //~ Instance Variables ---------------------------------------------------------------------------
//
//  private CloudWatchReporter reporter;
//  private String awsLogGroupName;
//  private String awsLogStreamFlushPeriodInSeconds;
//  private String awsLogStreamName;
//  private String awsRegionName;
//
//  //~ Constructors ---------------------------------------------------------------------------------
//
//  /**
//   * Creates a new CloudWatchAppenderSkeleton object.
//   */
//  public CloudWatchAppenderSkeleton()
//  {
//  }
//
//  //~ Methods --------------------------------------------------------------------------------------
//
//  /**
//   * DOCUMENT ME
//   */
//  @Override public void activateOptions()
//  {
//    this.reporter = new CloudWatchReporter(getAwsLogGroupName(), getAwsLogStreamName(), getAwsLogStreamFlushPeriodInSeconds(), getAwsRegionName());
//    this.reporter.start();
//  }
//
//
//  /**
//   * DOCUMENT ME
//   */
//  @Override public void close()
//  {
//    this.reporter.stop();
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @return DOCUMENT ME
//   */
//  public String getAwsLogGroupName()
//  {
//    return this.awsLogGroupName;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @return DOCUMENT ME
//   */
//  public String getAwsLogStreamFlushPeriodInSeconds()
//  {
//    return this.awsLogStreamFlushPeriodInSeconds;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @return DOCUMENT ME
//   */
//  public String getAwsLogStreamName()
//  {
//    return this.awsLogStreamName;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @return DOCUMENT ME
//   */
//  public String getAwsRegionName()
//  {
//    return this.awsRegionName;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @return DOCUMENT ME
//   */
//  @Override public boolean requiresLayout()
//  {
//    return true;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param aAwsLogGroupName DOCUMENT ME
//   */
//  public void setAwsLogGroupName(String aAwsLogGroupName)
//  {
//    this.awsLogGroupName = aAwsLogGroupName;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param aAwsLogStreamFlushPeriodInSeconds DOCUMENT ME
//   */
//  public void setAwsLogStreamFlushPeriodInSeconds(String aAwsLogStreamFlushPeriodInSeconds)
//  {
//    this.awsLogStreamFlushPeriodInSeconds = aAwsLogStreamFlushPeriodInSeconds;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param aAwsLogStreamName DOCUMENT ME
//   */
//  public void setAwsLogStreamName(String aAwsLogStreamName)
//  {
//    this.awsLogStreamName = aAwsLogStreamName;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param aAwsRegionName DOCUMENT ME
//   */
//  public void setAwsRegionName(String aAwsRegionName)
//  {
//    this.awsRegionName = aAwsRegionName;
//  }
//
//
//  /**
//   * DOCUMENT ME
//   *
//   * @param aEvent aArg0 DOCUMENT ME
//   */
//  @Override protected void append(LoggingEvent aEvent)
//  {
//    if (this.reporter != null)
//    {
//      String format = getLayout().format(aEvent);
//      long timeStamp = aEvent.getTimeStamp();
//      this.reporter.append(format, timeStamp);
//    }
//  }
//}
