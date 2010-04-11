
package org.globus.swift.data.policy;

import org.globus.cog.abstraction.interfaces.Status;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.cog.abstraction.coaster.service.job.manager.Hook;

public class AllocationHook extends Hook
{
    public void blockActive(StatusEvent e)
    {
        System.out.println("blockActive: " + e.getStatus().getMessage());
    }
}
