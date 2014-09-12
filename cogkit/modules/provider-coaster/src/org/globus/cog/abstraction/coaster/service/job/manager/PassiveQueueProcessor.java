/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.globus.cog.abstraction.coaster.service.LocalTCPService;
import org.globus.cog.abstraction.coaster.service.ResourceUpdateCommand;
import org.globus.cog.coaster.channels.CoasterChannel;

public class PassiveQueueProcessor extends BlockQueueProcessor {

    private boolean started = false;

    private final URI callbackURI;

    private int currentWorkers;

    public PassiveQueueProcessor(LocalTCPService localService, URI callbackURI) {
        super(localService, new Settings());
        setName("Passive Queue Processor");
        this.callbackURI = callbackURI;
        localService.setSingleQP(this);
    }

    @Override
    public void setBroadcaster(Broadcaster b) {
        super.setBroadcaster(b);
        try {
            RemoteLogCommand cmd = new RemoteLogCommand(RemoteLogCommand.Type.STDERR,
                "Passive queue processor initialized. Callback URI is " + callbackURI);
            b.send(cmd);
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
    public String registrationReceived(String blockID, String workerID, String workerHostname,
            CoasterChannel channel, Map<String, String> options) {

        String r = getBlock(blockID).workerStarted(workerID, workerHostname, channel, options);

        ResourceUpdateCommand wsc;
        synchronized(this) {
            currentWorkers++;
            wsc = new ResourceUpdateCommand("job-capacity",
                String.valueOf(currentWorkers * getSettings().getJobsPerNode()));
        }
        try {
            wsc.executeAsync(channel);
        }
        catch (Exception e) {
            logger.info("Failed to send worker status update to client", e);
        }

        return r;
    }

    @Override
    protected Block getBlock(String id) {
        Map<String, Block> blocks = getBlocks();
        Block b;
        synchronized(blocks) {
            b = blocks.get(id);
            if (b == null) {
                b = new Block(id, 1, TimeInterval.FOREVER, this);
                getLocalService().registerBlock(b, this);
                b.setStartTime(Time.now());
                b.setRunning(true);
                blocks.put(id, b);
            }
        }
        return b;
    }

    @Override
    public void nodeRemoved(Node node) {
        ResourceUpdateCommand wsc;
        synchronized(this) {
            currentWorkers--;
            wsc = new ResourceUpdateCommand("job-capacity",
                String.valueOf(node.getConcurrency()));
            if (node.getBlock().getNodes().isEmpty()) {
                getBlocks().remove(node.getBlock().getId());
            }
        }
        try {
            getBroadcaster().send(wsc);
        }
        catch (Exception e) {
            logger.warn("Failed to send worker status update to client", e);
        }
    }

    @Override
    public synchronized void start() {
        // TODO: workaround for double start problem. Unsure if final solution.
        if (!this.started) {
            super.start();
            this.started = true;
        }
    }
}
