//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.impl.execution.coaster.ServiceConfigurationCommand;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class ServiceConfigurationHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(ServiceConfigurationHandler.class);

    public static final String NAME = ServiceConfigurationCommand.NAME;

    public void requestComplete() throws ProtocolException {
        CoasterService service = (CoasterService) getChannel().getChannelContext().getService();
        JobQueue q = service.createJobQueue();
        Settings settings = q.getSettings();

        try {
            List<byte[]> l = getInDataChunks();
            if (l != null) {
                for (byte[] b : l) {
                    String s = new String(b);
                    String[] p = s.split("=", 2);
                    settings.set(p[0], p[1]);
                }
            }
            logger.debug(settings);
            sendReply(q.getId());
        }
        catch (Exception e) {
            logger.warn("Failed to set configuration", e);
            sendError("Failed to set configuration: " + e.getMessage(), e);
        }
    }
}
