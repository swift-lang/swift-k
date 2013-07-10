//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import org.apache.log4j.Logger;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class ServiceShutdownHandler extends RequestHandler {
    public static final Logger logger = Logger
            .getLogger(ServiceShutdownHandler.class);

    public static final String NAME = "SHUTDOWNSERVICE";

    public void requestComplete() throws ProtocolException {
        try {
            CoasterService cs = (CoasterService) getChannel()
                    .getChannelContext().getService();
            sendReply("OK");
            Thread.sleep(100);
            cs.shutdown();
        }
        catch (Exception e) {
            logger.warn("Failed to shut down service", e);
            throw new ProtocolException("Failed to shut down service", e);
        }
    }
}
