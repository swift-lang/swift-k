package org.griphyn.vdl.karajan.lib;

import org.globus.cog.karajan.workflow.nodes.Sequential;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.ThreadingContext;
import org.globus.cog.karajan.workflow.ExecutionException;


/** launch a single child, giving it a new thread ID, but without starting
    an entire karajan thread
*/

public class SequentialWithID extends Sequential {

    protected void executeChildren(VariableStack stack) throws ExecutionException {
        ThreadingContext tc = (ThreadingContext) stack.getVar("#thread");
        stack.setVar("#thread", tc.split(1));
        super.executeChildren(stack);
    }

}

