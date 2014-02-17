//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service.local;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.Registering;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.AbstractStreamCoasterChannel;
import org.globus.cog.coaster.channels.ChannelContext;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.handlers.RequestHandler;

public class RegistrationHandler extends RequestHandler {

    Logger logger = Logger.getLogger(RegistrationHandler.class);

    public static final String NAME = "REGISTER";

    @Override
    public void requestComplete() throws ProtocolException {
        String id = this.getInDataAsString(0);
        String url = this.getInDataAsString(1);
        
        Map<String, String> options;
        
        if (getInDataChunks().size() > 2) {
        	options = new HashMap<String, String>();
        	String opts = this.getInDataAsString(2);
        	String[] pairs = opts.split(",");
        	for (String p : pairs) {
        		String[] kv = p.split("=");
        		options.put(kv[0].trim(), kv[1].trim());
        	}
        }
        else {
        	options = Collections.emptyMap();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("registering: " + id + " " + url);
        }

        CoasterChannel channel = getChannel();
        ChannelContext context = channel.getChannelContext();
        Registering ls = (Registering) context.getService();
        try {
            String rid = ls.registrationReceived(id, url, channel, options);
            if (channel instanceof AbstractStreamCoasterChannel) {
                AbstractStreamCoasterChannel askc =
                    (AbstractStreamCoasterChannel) channel;
                String s = id + (rid == null ? "" : "-" + rid);
                URI uri = new URI(s);
            	askc.setContact(uri);
            }
            this.sendReply(rid == null ? "OK" : rid);
        }
        catch (Exception e) {
            throw new ProtocolException(e);
        }
    }
}
