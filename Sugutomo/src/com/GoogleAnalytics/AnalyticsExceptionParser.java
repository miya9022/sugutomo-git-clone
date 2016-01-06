package com.GoogleAnalytics;

import java.io.PrintWriter;
import java.io.StringWriter;


import com.google.analytics.tracking.android.ExceptionParser;
 
public class AnalyticsExceptionParser implements ExceptionParser {
  /*
   * (non-Javadoc)
   * @see com.google.analytics.tracking.android.ExceptionParser#getDescription(java.lang.String, java.lang.Throwable)
   */
  public String getDescription(String p_thread, Throwable p_throwable) {
		          final StringWriter sw = new StringWriter();
		          final PrintWriter pw = new PrintWriter(sw, true);
		          p_throwable.printStackTrace(pw);
     return "Thread: " + p_thread + ", Exception: " + sw.getBuffer().toString();
  }
}
