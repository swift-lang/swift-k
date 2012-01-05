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

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.Registering;
import org.globus.cog.karajan.workflow.service.ProtocolException;
import org.globus.cog.karajan.workflow.service.channels.AbstractStreamKarajanChannel;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;
import org.globus.cog.karajan.workflow.service.handlers.RequestHandler;

public class RegistrationHandler extends RequestHandler {

    Logger logger = Logger.getLogger(RegistrationHandler.class);

    public static final String NAME = "REGISTER";

    @Override
    public void requestComplete() throws ProtocolException {
        String id = this.getInDataAsString(0);
        String url = this.getInDataAsString(1);

        logger.debug("registering: " + id + " " + url);

        KarajanChannel channel = getChannel();
        ChannelContext context = channel.getChannelContext();
        Registering ls = (Registering) context.getService();
        try {
            String rid = ls.registrationReceived(id, url, channel);
            if (channel instanceof AbstractStreamKarajanChannel) {
                AbstractStreamKarajanChannel askc =
                    (AbstractStreamKarajanChannel) channel;
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
