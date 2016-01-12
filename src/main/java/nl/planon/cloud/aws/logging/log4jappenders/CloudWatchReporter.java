// Planon Enterprise Edition Source file: CloudWatchReporter.java
// Copyright Planon 1997-2016. All Rights Reserved.
package nl.planon.cloud.aws.logging.log4jappenders;

import com.amazonaws.auth.*;
import com.amazonaws.regions.*;
import com.amazonaws.services.logs.*;
import com.amazonaws.services.logs.model.*;

import java.util.*;
import java.util.concurrent.*;

import static nl.planon.cloud.aws.logging.log4jappenders.Config.*;

/**
 * CloudWatchReporter
 */
public class CloudWatchReporter
{
  //~ Static Variables & Initializers --------------------------------------------------------------

  private static final String AWS_INSTANCE_ID; // per-instance, so static

  static
  {
    AWS_INSTANCE_ID = retrieveInstanceId();
  }

  //~ Instance Variables ---------------------------------------------------------------------------

  private AWSLogsClient awsLogsClient = null;

  private final BlockingQueue<InputLogEvent> queue = new LinkedBlockingQueue<>(AWS_LOG_STREAM_MAX_QUEUE_DEPTH);
  private volatile boolean queueFull = false;
  private volatile boolean shutdown = false;
  private final int flushPeriodMillis;
  private long lastReportedTimestamp = -1;
  private final Object monitor = new Object();

  private String logGroupName;
  private String logStreamName;

  private final Runnable messageProcessor = new Runnable()
  {
    @Override public void run()
    {
      debug("Draining queue for " + CloudWatchReporter.this.logStreamName + " stream every " + (CloudWatchReporter.this.flushPeriodMillis / 1000) + "s...");
      while (!CloudWatchReporter.this.shutdown)
      {
        try
        {
          flush();
        }
        catch (Throwable t)
        {
          t.printStackTrace();
        }
        if (!CloudWatchReporter.this.shutdown && (CloudWatchReporter.this.queue.size() < AWS_DRAIN_LIMIT))
        {
          try
          {
            synchronized (CloudWatchReporter.this.monitor)
            {
              CloudWatchReporter.this.monitor.wait(CloudWatchReporter.this.flushPeriodMillis);
            }
          }
          catch (InterruptedException ix)
          {
            ix.printStackTrace();
          }
        }
      }

      while (!CloudWatchReporter.this.queue.isEmpty())
      {
        flush();
      }
    }
  };

  private String sequenceTokenCache = null; // aws doc: "Every PutLogEvents request must include the sequenceToken obtained from the response of the previous request.

  private Thread deliveryThread;

  //~ Constructors ---------------------------------------------------------------------------------

  /**
   * Creates a new CloudWatchReporter object.
   *
   * @param awsLogGroupName                  DOCUMENT ME
   * @param awsLogStreamName                 DOCUMENT ME
   * @param awsLogStreamFlushPeriodInSeconds DOCUMENT ME
   * @param aRegion                          DOCUMENT ME
   */
  public CloudWatchReporter(final String awsLogGroupName, final String awsLogStreamName, final String awsLogStreamFlushPeriodInSeconds, String aRegion)
  {
    // figure out the flush period
    int flushPeriod = AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS;
    if (awsLogStreamFlushPeriodInSeconds != null)
    {
      try
      {
        flushPeriod = Integer.parseInt(awsLogStreamFlushPeriodInSeconds);
      }
      catch (NumberFormatException nfe)
      {
        debug("Bad awsLogStreamFlushPeriodInSeconds (" + awsLogStreamFlushPeriodInSeconds + "), defaulting to: " + AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS + "s");
      }
    }
    else
    {
      debug("No awsLogStreamFlushPeriodInSeconds specified, defaulted to " + AWS_LOG_STREAM_FLUSH_PERIOD_IN_SECONDS + "s");
    }
    this.flushPeriodMillis = flushPeriod * 1000;

    try
    {
      this.awsLogsClient = new AWSLogsClient(); // this should pull the credentials automatically from the environment
      try
      {
        this.awsLogsClient.setRegion(Region.getRegion(Regions.fromName(aRegion)));
      }
      catch (IllegalArgumentException e)
      {
        debug("Invalid region: " + aRegion + " using default eu-central-1");
        this.awsLogsClient.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
      }
      // set the group name
      this.logGroupName = awsLogGroupName;

      String logStreamNamePrefix = awsLogStreamName;
      if (logStreamNamePrefix == null)
      {
        logStreamNamePrefix = ENV_LOG_STREAM_NAME;
      }
      if (logStreamNamePrefix == null)
      {
        logStreamNamePrefix = AWS_INSTANCE_ID;
      }
      String finalLogStreamName;

      finalLogStreamName = logStreamNamePrefix;
      this.sequenceTokenCache = createLogGroupAndLogStreamIfNeeded(this.logGroupName, finalLogStreamName);

      this.logStreamName = finalLogStreamName;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  //~ Methods --------------------------------------------------------------------------------------

  //private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss"); // aws doesn't allow ":" in stream name
  /**
   * DOCUMENT ME
   *
   * @param aEvent     DOCUMENT ME
   * @param aTimestamp DOCUMENT ME
   */
  public void append(final String aEvent, long aTimestamp)
  {
    final InputLogEvent awsLogEvent = new InputLogEvent();

    final String message = aEvent;
    awsLogEvent.setTimestamp(Long.valueOf(aTimestamp));
    awsLogEvent.setMessage(message);
    if (!this.queue.offer(awsLogEvent) && !this.queueFull)
    {
      debug("Log queue is full!");
      this.queueFull = true;
    }
    else if (this.queueFull)
    {
      this.queueFull = false;
    }
  }


  /**
   * DOCUMENT ME
   */
  public void start()
  {
    DefaultAWSCredentialsProviderChain defaultAWSCredentialsProviderChain = new DefaultAWSCredentialsProviderChain();
    debug("Starting cloudWatchAppender for: " + this.logGroupName + ":" + this.logStreamName + " on account " + defaultAWSCredentialsProviderChain.getCredentials().getAWSAccessKeyId());
    this.deliveryThread = new Thread(this.messageProcessor, "CloudWatchAppenderDeliveryThread");
    this.deliveryThread.start();
    debug("Starting cloudWatchAppender for: " + this.logGroupName + ":" + this.logStreamName + " on account " + defaultAWSCredentialsProviderChain.getCredentials().getAWSAccessKeyId());
  }


  /**
   * DOCUMENT ME
   */
  public void stop()
  {
    this.shutdown = true;
    if (this.deliveryThread != null)
    {
      synchronized (this.monitor)
      {
        this.monitor.notify();
      }
      try
      {
        this.deliveryThread.join(SHUTDOWN_TIMEOUT_MILLIS);
      }
      catch (InterruptedException ix)
      {
        ix.printStackTrace();
      }
    }
    if (this.queue.size() > 0)
    {
      flush();
    }
  }


  /**
   * Create log group ans log stream if needed.
   *
   * @param  logGroupName  the name of the log group
   * @param  logStreamName the name of the stream
   *
   * @return sequence token for the created stream
   */
  private String createLogGroupAndLogStreamIfNeeded(String logGroupName, String logStreamName)
  {
    debug("Using log group " + logGroupName);
//    final DescribeLogGroupsResult describeLogGroupsResult = this.awsLogsClient.describeLogGroups(new DescribeLogGroupsRequest().withLogGroupNamePrefix(logGroupName));
//    System.out.println("Log bucket result: " + describeLogGroupsResult);
//    boolean createLogGroup = true;
//    if (describeLogGroupsResult != null && describeLogGroupsResult.getLogGroups() != null && !describeLogGroupsResult.getLogGroups().isEmpty())
//    {
//      for (final LogGroup lg : describeLogGroupsResult.getLogGroups())
//      {
//        if (logGroupName.equals(lg.getLogGroupName()))
//        {
//          createLogGroup = false;
//          break;
//        }
//      }
//    }
//    if (createLogGroup)
//    {
//      debug("Creating logGroup: " + logGroupName);
//      final CreateLogGroupRequest createLogGroupRequest = new CreateLogGroupRequest(logGroupName);
//      this.awsLogsClient.createLogGroup(createLogGroupRequest);
//    }

    String logSequenceToken = null;
    boolean createLogStream = true;
    final DescribeLogStreamsRequest describeLogStreamsRequest = new DescribeLogStreamsRequest(logGroupName).withLogStreamNamePrefix(logStreamName);
    final DescribeLogStreamsResult describeLogStreamsResult = this.awsLogsClient.describeLogStreams(describeLogStreamsRequest);
    debug("Starting search fo existing buckets...");
    if ((describeLogStreamsResult != null) && (describeLogStreamsResult.getLogStreams() != null) && !describeLogStreamsResult.getLogStreams().isEmpty())
    {
      for (final LogStream ls : describeLogStreamsResult.getLogStreams())
      {
        debug("Found a bucket named " + ls.getLogStreamName());
        if (logStreamName.equals(ls.getLogStreamName()))
        {
          createLogStream = false;
          logSequenceToken = ls.getUploadSequenceToken();
        }
      }
    }

    if (createLogStream)
    {
      debug("Creating logStream: " + logStreamName);
      final CreateLogStreamRequest createLogStreamRequest = new CreateLogStreamRequest(logGroupName, logStreamName);
      this.awsLogsClient.createLogStream(createLogStreamRequest);
    }
    return logSequenceToken;
  }


  /**
   * DOCUMENT ME
   *
   * @param s DOCUMENT ME
   */
  private void debug(final String s)
  {
    System.err.println(" CloudWatchAppender: " + s);
  }


  /**
   * DOCUMENT ME
   */
  private void flush()
  {
    int drained;
    final List<InputLogEvent> logEvents = new ArrayList<>(AWS_DRAIN_LIMIT);
    do
    {
      drained = this.queue.drainTo(logEvents, AWS_DRAIN_LIMIT);
      if (logEvents.isEmpty())
      {
        break;
      }
      Collections.sort(logEvents, new Comparator<InputLogEvent>()
        {
          @Override public int compare(InputLogEvent o1, InputLogEvent o2)
          {
            return o1.getTimestamp().compareTo(o2.getTimestamp());
          }
        });
      if (this.lastReportedTimestamp > 0)
      {
        //in the off chance that the new events start with older TS than the last sent event
        //reset their timestamps to the last timestamp until we reach an event with
        //higher timestamp
        for (InputLogEvent event : logEvents)
        {
          if (event.getTimestamp().longValue() < this.lastReportedTimestamp)
          {
            event.setTimestamp(Long.valueOf(this.lastReportedTimestamp));
          }
          else
          {
            break;
          }
        }
      }
      boolean reported = false;
      int maxLoops = 10;
      int loops = 0;
      while (!reported && (loops < maxLoops))
      {
        loops++;
        this.lastReportedTimestamp = logEvents.get(logEvents.size() - 1).getTimestamp().longValue();
        final PutLogEventsRequest putLogEventsRequest = new PutLogEventsRequest(this.logGroupName, this.logStreamName, logEvents);
        putLogEventsRequest.setSequenceToken(this.sequenceTokenCache);
        reported = true;
        try
        {
          final PutLogEventsResult putLogEventsResult = this.awsLogsClient.putLogEvents(putLogEventsRequest); // 1 MB or 10000 messages AWS cap!
          this.sequenceTokenCache = putLogEventsResult.getNextSequenceToken();
        }
        catch (final DataAlreadyAcceptedException daae)
        {
          //debug("DataAlreadyAcceptedException, will reset the token to the expected one");
          this.sequenceTokenCache = daae.getExpectedSequenceToken();
        }
        catch (final InvalidSequenceTokenException iste)
        {
          //debug("InvalidSequenceTokenException, will reset the token to the expected one");
          this.sequenceTokenCache = iste.getExpectedSequenceToken();
        }
        catch (Exception e)
        {
          debug("Error writing logs");
          e.printStackTrace();
        }
      }
      logEvents.clear();
    }
    while (drained >= AWS_DRAIN_LIMIT);
  }
}
