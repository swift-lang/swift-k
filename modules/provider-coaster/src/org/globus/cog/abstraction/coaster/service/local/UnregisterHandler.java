//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service.local;

import org.globus.cog.abstraction.coaster.service.Registering;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.handlers.RequestHandler;


public class UnregisterHandler extends RequestHandler {
    public static final String NAME = "UNREGISTER";
	
	public void requestComplete() throws ProtocolException {
	    String id = this.getInDataAsString(0);
        Registering ls = (Registering) 
            getChannel().getChannelContext().getService();
        try {
            ls.unregister(id);
            this.sendReply("OK");
        }
        catch (Exception e) {
            throw new ProtocolException(e);
        }
	}
}
