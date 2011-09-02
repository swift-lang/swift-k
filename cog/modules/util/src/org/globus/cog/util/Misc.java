
//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

package org.globus.cog.util;

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class Misc
{
	/** 
	   Set the FileAppender output from the given Logger 
	   to the given file name
	 */
	public static void setFileAppenderOutput(Logger logger, 
	                                         String filename) {
		File file = new File(filename);
		FileAppender fa = 
			(FileAppender) Misc.getAppender(FileAppender.class);
		if (fa == null) {
			logger.fatal("Failed to configure log file name");
			System.exit(2);
		}
		fa.setFile(file.getAbsolutePath());
		fa.activateOptions();
	}
	
	/**
	   Get an Appender of given type
	   Useful when renaming the log file name 
	 */
	public static Appender getAppender(Class<?> cls) {
        Logger root = Logger.getRootLogger();
        Enumeration<?> e = root.getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (cls.isAssignableFrom(a.getClass())) {
                return a;
            }
        }
        return null;
    }
}
