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
import java.util.Map;

import org.globus.cog.abstraction.coaster.rlog.RemoteLogCommand;
import org.globus.cog.abstraction.coaster.service.RegistrationManager;
import org.globus.cog.karajan.workflow.service.channels.ChannelContext;
import org.globus.cog.karajan.workflow.service.channels.ChannelManager;
import org.globus.cog.karajan.workflow.service.channels.KarajanChannel;

public class PassiveQueueProcessor extends BlockQueueProcessor implements RegistrationManager {
    private final URI callbackURI;
    
    public PassiveQueueProcessor(Settings settings, URI callbackURI) {
        super(settings);
        setName("Passive Queue Processor");
        setSettings(settings);
        this.callbackURI = callbackURI;
    }
    
    @Override
    public void setClientChannelContext(ChannelContext channelContext) {
        super.setClientChannelContext(channelContext);
        KarajanChannel channel;
        try {
            channel = ChannelManager.getManager().reserveChannel(channelContext);
            RemoteLogCommand cmd = new RemoteLogCommand(RemoteLogCommand.Type.STDERR, 
                "Passive queue processor initialized. Callback URI is " + callbackURI);
            cmd.executeAsync(channel, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int updatePlan() throws PlanningException {
        return 1;
    }

    @Override
    protected void removeIdleBlocks() {
        // no removing of idle blocks here
    }

    @Override
    protected Block getBlock(String id) {
        Map<String, Block> blocks = getBlocks();
        synchronized(blocks) {
            Block b = blocks.get(id);
            if (b == null) {
                b = new Block(id, 1, TimeInterval.FOREVER, this);
                b.setRunning(true);
                blocks.put(id, b);
            }
            return b;
        }
    }
}
