//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 8, 2011
 */
package org.globus.cog.abstraction.coaster.service;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.local.LocalService;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;

public class ResourceUpdateHandler extends RequestHandler { 
    public static Logger logger = Logger.getLogger(ResourceUpdateHandler.class);
    
    @Override
    public void requestComplete() throws ProtocolException {
        LocalService ls = (LocalService) getChannel().getChannelContext().getService();
        if (ls == null) {
            // getting this on a client channel, so just log this
            logger.info(getInDataAsString(0) + ": " + getInDataAsString(1));
        }
        else {
            ls.resourceUpdated(getChannel().getChannelContext(), 
                getInDataAsString(0), getInDataAsString(1));
        }
        sendReply("OK");
    }
}
