/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class Misc
{
	/** 
	 * Set the FileAppender output from the given Logger 
	 * to the given file name. If no file appender exists, one
	 * is created.
	 */
	public static void setFileAppenderOutput(Logger logger, 
	                                         String filename) {
		File file = new File(filename);
		FileAppender fa = 
			(FileAppender) Misc.getAppender(FileAppender.class);
		if (fa == null) {
		    fa = new FileAppender();
		    Layout l = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss,SSSZZZZZ} %-5p %c{1} %m%n");
		    fa.setLayout(l);
		    fa.setThreshold(Level.DEBUG);
		    Logger.getRootLogger().addAppender(fa);
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
