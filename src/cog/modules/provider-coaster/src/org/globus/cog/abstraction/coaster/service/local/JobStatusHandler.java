//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service.local;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.impl.common.StatusImpl;
import org.globus.cog.abstraction.impl.common.execution.JobException;
import org.globus.cog.abstraction.impl.execution.coaster.NotificationManager;
import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class JobStatusHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(JobStatusHandler.class);
    
    public static final String NAME = "JOBSTATUS";
    
    public void requestComplete() throws ProtocolException {
        try {
            String jobId = getInDataAsString(0);
            int status = Integer.parseInt(getInDataAsString(1));
            int code = Integer.parseInt(getInDataAsString(2));
            String message = getInDataAsString(3);
            
            Status s = new StatusImpl();
            s.setStatusCode(status);
            if (status == Status.FAILED && code != 0) {
                s.setException(new JobException(code));
            }
            if (message != null && !message.equals("")) {
                s.setMessage(message);
            }
            NotificationManager.getDefault().notificationReceived(jobId, s);
            sendReply("OK");
        }
        catch (Exception e) {
            throw new ProtocolException("Could not deserialize job status", e);
        }
    }
}
