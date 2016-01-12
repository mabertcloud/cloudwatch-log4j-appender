package nl.planon.cloud.aws.logging.log4jappenders;

import java.util.logging.*;

public class LogManagerHelper
{
	 /**
   * Get the value of a logging property.
   * The method returns null if the property is not found.
   * @param name      property name
   * @return          property value
   */
  public static String getProperty(LogManager manager, String name) {
      return manager.getProperty(name);
  }

  // Package private method to get a String property.
  // If the property is not defined we return the given
  // default value.
  public static String getStringProperty(LogManager manager,String name, String defaultValue) {
      String val = getProperty(manager, name);
      if (val == null) {
          return defaultValue;
      }
      return val.trim();
  }

  // Package private method to get an integer property.
  // If the property is not defined or cannot be parsed
  // we return the given default value.
  public static int getIntProperty(LogManager manager,String name, int defaultValue) {
      String val = getProperty(manager, name);
      if (val == null) {
          return defaultValue;
      }
      try {
          return Integer.parseInt(val.trim());
      } catch (Exception ex) {
          return defaultValue;
      }
  }

  // Package private method to get a boolean property.
  // If the property is not defined or cannot be parsed
  // we return the given default value.
  public static boolean getBooleanProperty(LogManager manager,String name, boolean defaultValue) {
      String val = getProperty(manager, name);
      if (val == null) {
          return defaultValue;
      }
      val = val.toLowerCase();
      if (val.equals("true") || val.equals("1")) {
          return true;
      } else if (val.equals("false") || val.equals("0")) {
          return false;
      }
      return defaultValue;
  }

  // Package private method to get a Level property.
  // If the property is not defined or cannot be parsed
  // we return the given default value.
  public static Level getLevelProperty(LogManager manager,String name, Level defaultValue) {
      String val = getProperty(manager, name);
      if (val == null) {
          return defaultValue;
      }
      try {
          return Level.parse(val.trim());
      } catch (Exception ex) {
          return defaultValue;
      }
  }

  // Package private method to get a filter property.
  // We return an instance of the class named by the "name"
  // property. If the property is not defined or has problems
  // we return the defaultValue.
  public static Filter getFilterProperty(LogManager manager,String name, Filter defaultValue) {
      String val = getProperty(manager, name);
      try {
          if (val != null) {
              Class clz = ClassLoader.getSystemClassLoader().loadClass(val);
              return (Filter) clz.newInstance();
          }
      } catch (Exception ex) {
          // We got one of a variety of exceptions in creating the
          // class or creating an instance.
          // Drop through.
      }
      // We got an exception.  Return the defaultValue.
      return defaultValue;
  }


  // Package private method to get a formatter property.
  // We return an instance of the class named by the "name"
  // property. If the property is not defined or has problems
  // we return the defaultValue.
  public static Formatter getFormatterProperty(LogManager manager,String name, Formatter defaultValue) {
      String val = getProperty(manager, name);
      try {
          if (val != null) {
              Class clz = ClassLoader.getSystemClassLoader().loadClass(val);
              return (Formatter) clz.newInstance();
          }
      } catch (Exception ex) {
          // We got one of a variety of exceptions in creating the
          // class or creating an instance.
          // Drop through.
      }
      // We got an exception.  Return the defaultValue.
      return defaultValue;
  }
}
