//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Jul 13, 2012
 */
package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.griphyn.vdl.karajan.WaitingThreadsMonitor;

public class UnitEnd extends FlowNode {    
    public static final Arg.Positional TYPE = new Arg.Positional("type");
    public static final Arg.Optional NAME = new Arg.Optional("name", null);
    public static final Arg.Optional LINE = new Arg.Optional("line", null);
    
    @Override
    public void execute(VariableStack stack) throws ExecutionException {
        executeSimple(stack);
        complete(stack);
    }
    
    @Override
    public boolean isSimple() {
        return super.isSimple();
    }
    
    @Override
    public void executeSimple(VariableStack stack) throws ExecutionException {
        String type = (String) TYPE.getStatic(this);
        ThreadingContext thread = ThreadingContext.get(stack);
        String name = (String) NAME.getStatic(this);
        String line = (String) LINE.getStatic(this);
        
        UnitStart.log(false, type, thread, name, line);
        WaitingThreadsMonitor.removeOutput(stack);
    }
}
