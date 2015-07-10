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
 * Created on Jul 2, 2014
 */
package org.globus.cog.abstraction.coaster.service;

import java.util.HashMap;
import java.util.Map;

import org.globus.cog.abstraction.coaster.service.job.manager.AbstractBlockWorkerManager;
import org.globus.cog.abstraction.coaster.service.job.manager.BlockQueueProcessor;
import org.globus.cog.coaster.channels.CoasterChannel;

public class BlockRegistry implements RegistrationManager {
    private Map<String, AbstractBlockWorkerManager> managers;
    private BlockQueueProcessor singleQP; 
    
    public BlockRegistry() {
        this.managers = new HashMap<String, AbstractBlockWorkerManager>();
    }
    
    public void addBlock(String blockId, AbstractBlockWorkerManager bqp) {
        synchronized(managers) {
            managers.put(blockId, bqp);
        }
    }
    
    private AbstractBlockWorkerManager get(String blockId) {
        if (singleQP != null) {
            return singleQP;
        }
        synchronized(managers) {
            AbstractBlockWorkerManager rm = managers.get(blockId);
            if (rm == null) {
                throw new IllegalArgumentException("No such block registered with dispatcher: " + blockId);
            }
            return rm;
        }
    }
    
    public void removeBlock(String blockId) {
        synchronized(managers) {
            managers.remove(blockId);
        }
    }

    @Override
    public String registrationReceived(String blockID, String workerID, String workerHostname,
            CoasterChannel channel, Map<String, String> options) {
        return get(blockID).registrationReceived(blockID, workerID, workerHostname, channel, options);
    }

    @Override
    public String nextId(String id) {
        return get(id).nextId(id);
    }

    public AbstractBlockWorkerManager getQueueProcessor(String blockID) {
        return get(blockID);
    }

    public void setSingleQP(BlockQueueProcessor qp) {
        this.singleQP = qp;
    }
}
