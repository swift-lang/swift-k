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
