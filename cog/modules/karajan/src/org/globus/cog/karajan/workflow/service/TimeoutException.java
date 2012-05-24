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

import org.globus.cog.karajan.workflow.service.commands.Command;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class TimeoutException extends ProtocolException {
	private static final long serialVersionUID = -6781619140427115780L;

	public static final DateFormat DF = new SimpleDateFormat("yyMMdd-HHmmss.SSS");
	
	public TimeoutException(Command c, String msg) {
	    super(c + " " + msg + ". sendReqTime="
                    + DF.format(new Date(c.getSendReqTime())) + ", lastSendTime=" + DF.format(new Date(c.getSendTime()))
                    + ", now=" + DF.format(new Date()) + ", channel=" + c.getChannel());
    }
	
	public TimeoutException(RequestHandler h, String msg) {
        super(h + " " + msg + ". lastTime="
                    + DF.format(new Date(h.getLastTime()))
                    + ", now=" + DF.format(new Date()) + ", channel=" + h.getChannel());
    }
	
	public TimeoutException() {
		super();
	}
	
	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}
}
