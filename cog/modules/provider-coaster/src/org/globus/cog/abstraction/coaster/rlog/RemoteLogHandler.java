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

    public void requestComplete() throws ProtocolException {
        logger.info(getInDataAsString(0));
    }
}
