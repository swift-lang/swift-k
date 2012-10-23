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


/*
 * Created on Oct 14, 2012
 */
package org.griphyn.vdl.karajan.lib;

import java.io.IOException;
import java.util.LinkedList;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.Sequential;
import org.griphyn.vdl.util.VDL2Config;

public class Throttled extends Sequential {
    public static final int DEFAULT_MAX_THREADS = 1000000;
    
    private LinkedList<VariableStack> waiting;
    private int maxThreadCount, current;
    
    public Throttled() {
        try {
            maxThreadCount = TypeUtil.toInt(VDL2Config.getConfig()
                            .getProperty("exec.throttle", String.valueOf(DEFAULT_MAX_THREADS)));
        }
        catch (IOException e) {
            maxThreadCount = DEFAULT_MAX_THREADS;
        }
        current = 0;
        waiting = new LinkedList<VariableStack>();
    }

    @Override
    protected void executeChildren(VariableStack stack)
            throws ExecutionException {
        synchronized(this) {
            if (current == maxThreadCount) {
                waiting.addLast(stack);
                return;
            }
            else {
                current++;
            }
        }
        super.executeChildren(stack);
    }

    @Override
    protected void post(VariableStack stack) throws ExecutionException {
        synchronized(this) {
            if (!waiting.isEmpty()) {
                super.executeChildren(waiting.removeFirst());
            }
            else {
                current--;
            }
        }
        super.post(stack);
    }    
}
