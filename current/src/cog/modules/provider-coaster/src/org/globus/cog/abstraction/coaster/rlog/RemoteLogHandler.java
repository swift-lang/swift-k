//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 14, 2009
 */
package org.globus.cog.abstraction.coaster.rlog;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class RemoteLogHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(RemoteLogHandler.class);

    @Override
    public void requestComplete() throws ProtocolException {
        RemoteLogCommand.Type type = RemoteLogCommand.Type.valueOf(getInDataAsString(0));
        String msg = getInDataAsString(1);
        switch (type) {
            case STDOUT:
                System.out.println(msg);
                break;
            case STDERR:
                System.err.println(msg);
                break;
            case FATAL:
                logger.fatal(msg);
                break;
            case ERROR:
                logger.error(msg);
                break;
            case WARN:
                logger.warn(msg);
                break;
            case INFO:
                logger.info(msg);
                break;
            case DEBUG:
                logger.debug(msg);
                break;
        }
        sendReply("OK");
    }
}
