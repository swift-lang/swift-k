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
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class OutFiles extends AbstractSequentialWithArguments {
    public static final Arg STAGEOUTS = new Arg.Positional("stageouts");

    static {
        setArguments(OutFiles.class, new Arg[] { STAGEOUTS });
    }

    @Override
    protected void post(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEOUTS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        try {
            for (Object f : files) {
                List pv = TypeUtil.toList(f);
                Path p = Path.parse(TypeUtil.toString(pv.get(0)));
                DSHandle handle = (DSHandle) pv.get(1);
                DSHandle leaf = handle.getField(p);
                String fname = VDLFunction.filename(leaf)[0];
                ret.append(fname);
            }
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
        super.post(stack);
    }
}
