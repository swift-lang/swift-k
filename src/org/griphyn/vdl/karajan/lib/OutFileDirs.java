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
import org.griphyn.vdl.mapping.AbsFile;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.Path;

public class OutFileDirs extends VDLFunction {
    public static final Arg STAGEOUTS = new Arg.Positional("stageouts");

    static {
        setArguments(OutFileDirs.class, new Arg[] { STAGEOUTS });
    }

    @Override
    public Object function(VariableStack stack) throws ExecutionException {
        List files = TypeUtil.toList(STAGEOUTS.getValue(stack));
        VariableArguments ret = ArgUtil.getVariableReturn(stack);
        try {
            for (Object f : files) {
                List pv = TypeUtil.toList(f);
                Path p = parsePath(pv.get(0), stack);
                DSHandle handle = (DSHandle) pv.get(1);
                DSHandle leaf = handle.getField(p);
                String fname = VDLFunction.filename(leaf)[0];
                String dir = new AbsFile(fname).getDir();
                if (dir.startsWith("/") && dir.length() != 1) {
                    ret.append(dir.substring(1));
                }
                else if (dir.length() != 0) {
                    ret.append(dir);
                }
            }
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
        return null;
    }
}
