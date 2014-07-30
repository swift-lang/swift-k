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
 * Created on Jul 13, 2012
 */
package org.griphyn.vdl.karajan.lib;

import k.rt.ExecutionException;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;

public class UnitEnd extends InternalFunction {
    
    private ArgRef<String> type;
    private ArgRef<String> name;
    private ArgRef<Integer> line;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("type", optional("name", null), optional("line", -1)));
    }
    
    @Override
    public void run(LWThread thr) throws ExecutionException {
        String type = this.type.getValue();
        String name = this.name.getValue();
        int line = this.line.getValue();
        
        UnitStart.log(false, type, thr, name, line);
        WaitingThreadsMonitor.removeOutput(thr);
    }
}
