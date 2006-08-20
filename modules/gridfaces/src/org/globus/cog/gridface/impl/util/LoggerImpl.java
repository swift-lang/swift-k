//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------
package org.globus.cog.gridface.impl.util;

/*
 * Desktop logging system
 */

//Local imports
//Swing imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LoggerImpl extends JPanel implements Logger {

	/** Component to display log output, follows Log Level rules */
	private JTextArea output = null;
	/** This is keeps all the logs, regarless of the log level */
	private StringBuffer logBuffer = null;
	
	/** Setup default log level */
	private int logLevel = Logger.DEBUG;

	private static Hashtable myLoggers = new Hashtable();

	//Date format
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public LoggerImpl() {
		this(LoggerImpl.DEBUG);
	}
	public LoggerImpl(int logLevel) {
		this.setLevel(logLevel);
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		sdf.setTimeZone(TimeZone.getDefault());

		
		if (this.output == null) {
			this.logBuffer = new StringBuffer();
			this.output = new JTextArea();
			this.output.setEditable(false);
			this.info("Log started at " + sdf.format(cal.getTime()));
		}
		
		this.setLayout(new BorderLayout());
		JScrollPane logScroll = new JScrollPane(this.output);
		this.add(logScroll,BorderLayout.CENTER);
	}

	public static Logger createInstance(String name) {
		LoggerImpl newLogger = new LoggerImpl();
		myLoggers.put(name, newLogger);
		return newLogger;
	}

	public static Logger getInstance(String name) {
		if((LoggerImpl) myLoggers.get(name) == null){
			return createInstance(name);
		}else{
			return (LoggerImpl) myLoggers.get(name);
		}
		
	}

	public static void removeInstance(String name) {
		myLoggers.remove(name);
	}
	public void setOutput(JTextArea output) {
		this.output = output;
	}

	public JTextArea getOutput() {
		return this.output;
	}

	public void clearLog(){
		this.logBuffer = new StringBuffer();
		this.output.setText(null);
	}
	
	public void saveLogToFile(String fileName) throws IOException {
		if(fileName!=null){
	        BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName)));
	        out.write(this.logBuffer.toString());
	        out.close();
		}
	}
	public void showAboutFrame(Component parent){
		JOptionPane.showMessageDialog(
				parent,
				"GridDesktop Logger is part of the Java CoG Kit"
					+ " \n \n Authors:\n\tGregor von Laszewki"
					+ " <gregor@mcs.anl.gov>,\n\tPankaj Sahasrabudhe"
					+ "<pankaj@mcs.anl.gov>",
				"About GridDesktop Logger",
				JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * to write debugging messages which should not be printed when
	 * the application is in production.
	 */
	public void debug(String message) {
		String msg = "Debug> " + message + "\n";
		if (this.getLevel() <= Logger.DEBUG) {
			output.append(msg);
		}
		this.logBuffer.append(msg);
	}

	/**
	 * for messages similar to the "verbose" mode of many applications.
	 */
	public void info(String message) {
		String msg = "Info> " + message + "\n";
		if (this.getLevel() <= Logger.INFO) {
			output.append(msg);
		}
		this.logBuffer.append(msg);
	}

	/**
	 * for warning messages which are logged to some log
	 * but the application is able to carry on without a problem
	 */
	public void warn(String message) {
		String msg = "Warn> " + message + "\n";
		if (this.getLevel() <= Logger.WARN) {
			output.append(msg);
		}
		this.logBuffer.append(msg);
	}

	/**
	 * for application error messages which are also logged to some log but,
	 * still, the application can hobble along.
	 *
	 */
	public void error(String message) {
		String msg = "Error> " + message + "\n";
		//if (this.getLevel() <= Logger.ERROR) {
			output.append(msg);
		//}
		this.logBuffer.append(msg);
	}

	/**
	 * for critical messages, after logging of which the application quits abnormally.
	 *
	 */
	public void fatal(String message) {
		String msg = "Fatal> " + message + "\n";
		//if (this.getLevel() <= Logger.FATAL) {
			output.append(msg);
		//}
		this.logBuffer.append(msg);
	}

	/**
	 * Set log output level
	 */
	public void setLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * @return current log output level
	 */
	public int getLevel() {
		return this.logLevel;
	}
	
	/**
	 * returns the excepion and its stacktrace to a string
	 * @param exception
	 * @return
	 */
	public static String getExceptionString(Throwable exception) {
		if(exception == null) { return null; }
		StringWriter writer = new StringWriter();
	    exception.printStackTrace(new PrintWriter(writer));

		return writer.toString();
	}
}
