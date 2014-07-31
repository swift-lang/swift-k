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
 * Created on Aug 7, 2013
 */
package org.griphyn.vdl.karajan.monitor.processors.coasters;

import org.griphyn.vdl.karajan.monitor.SystemState;
import org.griphyn.vdl.karajan.monitor.items.StatefulItemClass;
import org.griphyn.vdl.karajan.monitor.processors.SimpleParser;

public class WorkerLostProcessor extends AbstractRemoteLogProcessor {
    private CoasterStatusItem item;
    
    @Override
    public void initialize(SystemState state) {
        super.initialize(state);
    }

    @Override
    public String getMessageHeader() {
        return "WORKER_LOST";
    }

    @Override
    public void processMessage(SystemState state, SimpleParser p, Object details) {
        try {
            p.skip("blockid=");
            String blockId = p.word();
            p.skip("id=");
            String workerId = p.word();
            
            CoasterStatusItem item = (CoasterStatusItem) state.getItemByID(CoasterStatusItem.ID, StatefulItemClass.MISC);
            item.workerLost(blockId, workerId);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
