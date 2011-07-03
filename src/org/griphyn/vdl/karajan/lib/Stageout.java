/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.Arg.Channel;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DataNode;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class Stageout extends VDLFunction {
    public static final Logger logger = Logger.getLogger(Stageout.class);

    public static final Arg VAR = new Arg.Positional("var");

    public static final Channel STAGEOUT = new Channel("stageout");
    public static final Channel RESTARTOUT = new Channel("restartout");

    static {
        setArguments(Stageout.class, new Arg[] { VAR });
    }

    private boolean isPrimitive(DSHandle var) {
        return (var instanceof AbstractDataNode && ((AbstractDataNode) var)
            .isPrimitive());
    }
    
    private List<?> list(Path p, DSHandle var) {
        ArrayList<Object> l = new ArrayList<Object>(2);
        l.add(p);
        l.add(var);
        return l;
    }

    protected Object function(VariableStack stack) throws ExecutionException {
        AbstractDataNode var = (AbstractDataNode) VAR.getValue(stack);
        boolean deperr = false;
        boolean mdeperr = false;
        // currently only static arrays are supported as app returns
        // however, previous to this, there was no code to check
        // if these arrays had their sizes closed, which could lead to 
        // race conditions (e.g. if this array's mapper had some parameter
        // dependencies that weren't closed at the time the app was started).
        if (var.getType().isArray()) {
            var.waitFor();
        }
        try {
            if (!isPrimitive(var)) {
                retPaths(STAGEOUT, stack, var);
            }
            if (var.isRestartable()) {
                retPaths(RESTARTOUT, stack, var);
            }
        }
        catch (MappingDependentException e) {
            logger.debug(e);
            deperr = true;
            mdeperr = true;
        }
        if (deperr || mdeperr) {
            NamedArguments named = ArgUtil.getNamedReturn(stack);
            named.add("deperror", deperr);
            named.add("mdeperror", mdeperr);
        }
        return null;
    }

    private void retPaths(Channel channel, VariableStack stack, DSHandle var) throws ExecutionException {
        try {
            Collection<Path> fp = var.getFringePaths();
            for (Path p : fp) {
                channel.ret(stack, list(p, var));
            }
        }
        catch (Exception e) {
            throw new ExecutionException(e);
        }
    }
}
