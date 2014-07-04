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

import org.globus.cog.abstraction.coaster.service.job.manager.BlockQueueProcessor;
import org.globus.cog.coaster.channels.CoasterChannel;

public class BlockRegistry implements RegistrationManager {
    private Map<String, BlockQueueProcessor> managers;
    
    public BlockRegistry() {
        this.managers = new HashMap<String, BlockQueueProcessor>();
    }
    
    public void addBlock(String blockId, BlockQueueProcessor bqp) {
        synchronized(managers) {
            managers.put(blockId, bqp);
        }
    }
    
    private BlockQueueProcessor get(String blockId) {
        synchronized(managers) {
            BlockQueueProcessor rm = managers.get(blockId);
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

    public BlockQueueProcessor getQueueProcessor(String blockID) {
        return get(blockID);
    }
}
