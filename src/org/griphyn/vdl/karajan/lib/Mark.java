/*
 * Created on Jul 18, 2010
 */
package org.griphyn.vdl.karajan.lib;

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataDependentException;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class Mark extends VDLFunction {
    public static final Arg RESTARTS = new Arg.Positional("restarts");
    public static final Arg ERR = new Arg.Positional("err");
    public static final Arg MAPPING = new Arg.Optional("mapping", Boolean.FALSE);

    static {
        setArguments(Mark.class, new Arg[] { RESTARTS, ERR, MAPPING });
    }

    @Override
    protected Object function(VariableStack stack) throws ExecutionException {
        try {
            if (TypeUtil.toBoolean(ERR.getValue(stack))) {
                boolean mapping = TypeUtil.toBoolean(MAPPING.getValue(stack));
                List files = TypeUtil.toList(RESTARTS.getValue(stack));
                for (Object f : files) {
                    List pv = TypeUtil.toList(f);
                    Path p = parsePath(pv.get(0), stack);
                    DSHandle handle = (DSHandle) pv.get(1);
                    DSHandle leaf = handle.getField(p);
                    synchronized (leaf) {
                        if (mapping) {
                            leaf.setValue(new MappingDependentException(leaf, null));
                        }
                        else {
                            leaf.setValue(new DataDependentException(leaf, null));
                        }
                        leaf.closeShallow();
                    }
                }
            }
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
        return null;
    }
}
