
package org.globus.swift.data.policy;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.coaster.service.job.manager.Hook;

import org.globus.swift.data.Director;

/**
 * Re-apply CDM policies when we obtain a new allocation from Coasters.
 * */
public class AllocationHook extends Hook
{
    public void blockActive(StatusEvent e)
    {
        if (!Director.isEnabled())
            return;
        
        System.out.println("blockActive: " + e.getStatus().getMessage());
        String msg = e.getStatus().getMessage();
        String[] tokens = msg.split("=");
        String allocation = tokens[1];
        Director.addAllocation(allocation);
    }
}
