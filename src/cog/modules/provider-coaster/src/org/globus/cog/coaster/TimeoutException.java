//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Aug 2, 2005
 */
package org.globus.cog.karajan.workflow.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class TimeoutException extends Exception {
	public static final DateFormat DF = new SimpleDateFormat("yyMMdd-HHmmss.SSS");
	
	public TimeoutException(String msg) {
	    super(msg);
    }
	
	public TimeoutException(KarajanChannel channel, String msg, long lastTime) {
        super(msg + ". lastTime="
                    + DF.format(new Date(lastTime))
                    + ", now=" + DF.format(new Date()) + ", channel=" + channel);
    }
}
