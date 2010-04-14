//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 5, 2010
 */
package org.globus.cog.abstraction.coaster.service.job.manager;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;

import org.globus.cog.abstraction.coaster.service.RegistrationManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;

public class PassiveQueueProcessor extends AbstractQueueProcessor implements RegistrationManager {
    private int id;
    private Set<ChannelContext> idle;
    
    public PassiveQueueProcessor(URI callbackURI) {
        super("Passive Queue Processor");
        System.out.println("Passive queue processor initialized. Callback URI is " + callbackURI);
    }
    
    public void setClientChannelContext(ChannelContext channelContext) {
    }

    private static final NumberFormat IDF = new DecimalFormat("0000");
    
    public synchronized String nextId(String id) {
        return IDF.format(this.id++);
    }

    public String registrationReceived(String id, String url, ChannelContext channelContext) {
        return null;
    }
}
