//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.BlockQueueProcessor;
import org.globus.cog.abstraction.coaster.service.job.manager.Settings;
import org.globus.cog.abstraction.impl.execution.coaster.ServiceConfigurationCommand;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class ServiceConfigurationHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(ServiceConfigurationHandler.class);

    public static final String NAME = ServiceConfigurationCommand.NAME;

    public void requestComplete() throws ProtocolException {
        Settings settings =
                ((BlockQueueProcessor) ((CoasterService) getChannel().getChannelContext().getService()).getJobQueue().getCoasterQueueProcessor()).getSettings();
        try {
            List l = getInDataChuncks();
            if (l != null) {
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    String s = new String((byte[]) i.next());
                    String[] p = s.split("=", 2);
                    settings.set(p[0], p[1]);
                }
            }
            sendReply("OK");
        }
        catch (Exception e) {
            logger.warn("Failed to set configuration", e);
            sendError("Failed to set configuration: " + e.getMessage(), e);
        }
    }
}
