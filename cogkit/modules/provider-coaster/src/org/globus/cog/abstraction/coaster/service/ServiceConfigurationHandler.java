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
 * Created on Jul 21, 2005
 */
package org.globus.cog.abstraction.coaster.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.globus.cog.abstraction.coaster.service.job.manager.AbstractSettings;
import org.globus.cog.abstraction.coaster.service.job.manager.JobQueue;
import org.globus.cog.abstraction.coaster.service.local.VersionHandler;
import org.globus.cog.abstraction.impl.execution.coaster.ServiceConfigurationCommand;
import org.globus.cog.coaster.ProtocolException;
import org.globus.cog.coaster.channels.ChannelManager;
import org.globus.cog.coaster.channels.CoasterChannel;
import org.globus.cog.coaster.handlers.RequestHandler;

public class ServiceConfigurationHandler extends RequestHandler {
    public static final Logger logger = Logger.getLogger(ServiceConfigurationHandler.class);

    public static final String NAME = ServiceConfigurationCommand.NAME;

    public void requestComplete() throws ProtocolException {
        String requiredVersion = getInDataAsString(0);
        
        if (requiredVersion.startsWith("requireVersion=")) {
            int ix = requiredVersion.indexOf('=');
            String vnum = requiredVersion.substring(ix + 1).trim();
            if (!vnum.equals(VersionHandler.VERSION)) {
                throw new ProtocolException("Client requires version " + vnum + 
                    ", but this service is version " + VersionHandler.VERSION);
            }
        }
        else {
            throw new ProtocolException("No version requirement information supplied by the client. Please update the client.");
        }
        
        CoasterChannel channel = getChannel();
        
        ChannelManager.getManager().registerChannel("id://" + channel.getID(), channel);
        
        CoasterService service = (CoasterService) channel.getService();
        
        if (service.isPersistent() && ((CoasterPersistentService) service).isShared()) {
            logger.info("Service is shared. Ignoring client configuration settings");
            JobQueue q = ((CoasterPersistentService) service).getSharedQueue();
            q.getBroadcaster().addChannel(channel);
            sendReply(q.getId());
        }
        else {
            JobQueue q = service.createJobQueue(channel);
            Map<String, String> settings = new HashMap<String, String>();
    
            try {
                List<byte[]> l = getInDataChunks();
                if (l != null) {
                    for (byte[] b : l.subList(1, l.size())) {
                        String s = new String(b);
                        String[] p = s.split("=", 2);
                        settings.put(p[0], p[1]);
                        //settings.set(p[0], p[1]);
                    }
                }
                
                q.initQueueProcessor(settings.get("workerManager"));
                AbstractSettings s = q.getQueueProcessor().getSettings();
                
                for (Map.Entry<String, String> e : settings.entrySet()) {
                    s.put(e.getKey(), e.getValue());
                }
                q.startQueueProcessor();
                
                logger.debug(s);
                sendReply(q.getId());
            }
            catch (Exception e) {
                logger.warn("Failed to set configuration", e);
                sendError("Failed to set configuration: " + e.getMessage(), e);
            }
        }
    }
}
