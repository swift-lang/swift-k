//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service.local;

import java.util.Date;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class JobStatusHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(JobStatusHandler.class);
    
    public static final String NAME = "JOBSTATUS";
    
    public void requestComplete() throws ProtocolException {
        try {
            String jobId = getInDataAsString(0);
            int status = getInDataAsInt(1);
            int code = getInDataAsInt(2);
            String message = getInDataAsString(3);
            
            String out = null, err = null;
            
            Status s = new StatusImpl();
            s.setStatusCode(status);
            if (status == Status.FAILED && code != 0) {
            	if (message != null && !message.equals("")) {
            		s.setException(new JobException(message, code));
            	}
            	else {
            	    s.setException(new JobException(code));
            	}
            }
            if (status == Status.FAILED || status == Status.COMPLETED) {
                if (getInDataSize() == 7) {
                    out = getInDataAsString(5);
                    err = getInDataAsString(6);
                }
            }
            if (message != null && !message.equals("")) {
                s.setMessage(message);
            }
            s.setTime(new Date(this.getInDataAsLong(4)));
            NotificationManager.getDefault().notificationReceived(jobId, s, out, err);
            sendReply("OK");
        }
        catch (Exception e) {
            throw new ProtocolException("Could not deserialize job status", e);
        }
    }
}
