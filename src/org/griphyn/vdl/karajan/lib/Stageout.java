/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.Arg.Channel;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
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
    
    private List list(Path p, DSHandle var) {
        ArrayList l = new ArrayList(2);
        l.add(p);
        l.add(var);
        return l;
    }

    protected Object function(VariableStack stack) throws ExecutionException {
        DSHandle var = (DSHandle) VAR.getValue(stack);
        boolean deperr = false;
        boolean mdeperr = false;
        try {
            if (!isPrimitive(var)) {
                retPaths(STAGEOUT, stack, var);
            }
            else if (var.isRestartable()) {
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
                channel.ret(stack, list(p, var.getField(p)));
            }
        }
        catch (HandleOpenException e) {
            throw new FutureNotYetAvailable(addFutureListener(stack, e
                .getSource()));
        }
        catch (InvalidPathException e) {
            throw new ExecutionException(e);
        }
    }
}
