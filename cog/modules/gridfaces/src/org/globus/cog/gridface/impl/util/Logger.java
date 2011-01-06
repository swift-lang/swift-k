//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.util;

import java.awt.Component;
import java.io.IOException;

/*
 * Logger interface.
 */

public interface Logger {
	public static final String sABOUT = "About logger";
	
	// Log output levels, default should be DEBUG
	public static final int DEBUG = 0;
	public static final int INFO = 1;
	public static final int WARN = 2;
	public static final int ERROR = 3;
	public static final int FATAL = 4;

	/**
	* to write debugging messages which should not be printed when the application is in production.
	*/
	public void debug(String message);

	/**
	 * for messages similar to the "verbose" mode of many applications.
	 *
	 */
	public void info(String message);

	/**
	* for warning messages which are logged to some
	* log but the application is able to carry on without a problem
	*
	*/
	public void warn(String message);

	/**
	 * for application error messages which are also logged to some log but,
	 * still, the application can hobble along.
	 *
	 */
	public void error(String message);

	/**
	 * for critical messages, after logging of which the application quits abnormally.
	 *
	 */
	public void fatal(String message);

	/**
	 * Set log output level
	 */
	public void setLevel(int logLevel);

	/**
	 * @return current log output level
	 */
	public int getLevel();

	/** display the about frame for this component */
	public void showAboutFrame(Component parent);
	
	public void clearLog();
	
	public void saveLogToFile(String fileName) throws IOException;
	
}
