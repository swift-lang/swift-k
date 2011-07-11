//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 8, 2011
 */
package org.globus.cog.abstraction.coaster.service;

import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class ResourceUpdateHandler extends RequestHandler {
    @Override
    public void requestComplete() throws ProtocolException {
        LocalService ls = (LocalService) getChannel().getChannelContext().getService();
        ls.resourceUpdated(getChannel().getChannelContext(), 
            getInDataAsString(0), getInDataAsString(1));
        sendReply("OK");
    }
}
