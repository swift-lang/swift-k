/*
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class DoRestartLog extends AbstractSequentialWithArguments {
    public static final Arg RESTARTOUTS = new Arg.Positional("restartouts");

    static {
        setArguments(DoRestartLog.class, new Arg[] { RESTARTOUTS });
    }

    @Override
    protected void post(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(RESTARTOUTS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        try {
            for (Object f : files) {
                List pv = TypeUtil.toList(f);
                Path p = Path.parse(TypeUtil.toString(pv.get(0)));
                DSHandle handle = (DSHandle) pv.get(1);
                LogVar.logVar(stack, handle, p);
            }
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
        super.post(stack);
    }
}
