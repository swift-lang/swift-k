/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.globus.swift.data.policy;

import org.globus.cog.abstraction.coaster.service.job.manager.Hook;
import org.globus.cog.abstraction.impl.common.StatusEvent;
import org.globus.swift.data.Director;

/**
 * Re-apply CDM policies when we obtain a new allocation from Coasters.
 * */
public class AllocationHook extends Hook
{
    public void blockActive(StatusEvent e, String blockId)
    {
        if (!Director.isEnabled())
            return;
        
        System.out.println("blockActive: " + blockId);
        Director.addAllocation(blockId);
    }
}
