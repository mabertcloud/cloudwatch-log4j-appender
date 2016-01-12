// ButAds Source file: CloudwatchTester.java
// Copyright ButAds 2011-2016. All Rights Reserved.
package nl.planon.cloud.aws.logging.log4jappenders;

/**
 * CloudwatchTester
 */
public class CloudwatchTester
{
  //~ Methods --------------------------------------------------------------------------------------

  /**
   * DOCUMENT ME
   *
   * @param  args DOCUMENT ME
   *
   * @throws InterruptedException DOCUMENT ME
   */
  public static void main(String[] args) throws InterruptedException
  {
    System.out.println("Create");
    CloudWatchReporter cloudWatchReporter = new CloudWatchReporter("Cockpit_test", "Cockpit", "5", "eu-central-1");
    System.out.println("Start");
    cloudWatchReporter.start();
    cloudWatchReporter.append("Start of test", System.currentTimeMillis());
    System.out.println("Message");
    cloudWatchReporter.append("Test message", System.currentTimeMillis());

    System.out.println("Loop");
    for (int i = 0; i < 10; i++)
    {
      cloudWatchReporter.append("Test message " + i, System.currentTimeMillis());
      Thread.sleep(1000 * 1);
    }
    System.out.println("Sleep");
    Thread.sleep(1000 * 10);
    System.out.println("Stop");
    cloudWatchReporter.append("Stop request received", System.currentTimeMillis());
    cloudWatchReporter.stop();
  }
}
