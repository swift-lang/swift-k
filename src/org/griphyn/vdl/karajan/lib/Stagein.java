/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.arguments.Arg.Channel;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureNotYetAvailable;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.HandleOpenException;
import org.griphyn.vdl.mapping.InvalidPathException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class Stagein extends VDLFunction {
    public static final Logger logger = Logger.getLogger(Stagein.class);

    public static final Arg VAR = new Arg.Positional("var");
    
    public static final Channel STAGEIN = new Channel("stagein");

    static {
        setArguments(Stagein.class, new Arg[] { VAR });
    }

    private boolean isPrimitive(DSHandle var) {
        return (var instanceof AbstractDataNode && ((AbstractDataNode) var)
            .isPrimitive());
    }

    private void waitFor(DSHandle var, VariableStack stack)
            throws ExecutionException {
        synchronized (var) {
            if (!var.isClosed()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Waiting for " + var);
                }
                throw new FutureNotYetAvailable(addFutureListener(stack, var));
            }
            else {
                Object v = var.getValue();
                if (logger.isDebugEnabled()) {
                    logger.debug("Do not need to wait for " +
                            var + " as it is closed and has value " + v +
                            (v != null ? " with class " + v.getClass() : ""));
                }
                if (v != null && v instanceof RuntimeException) {
                    throw (RuntimeException) v;
                }
            }
        }
    }

    protected Object function(VariableStack stack) throws ExecutionException {
        DSHandle var = (DSHandle) VAR.getValue(stack);
        if (!isPrimitive(var)) {
            boolean deperr = false;
            boolean mdeperr = false;
            try {
                Collection<Path> fp = var.getFringePaths();
                try {
                    for (Path p : fp) {
                        waitFor(var.getField(p), stack);
                    }
                }
                catch (DependentException e) {
                    deperr = true;
                }
                for (Path p : fp) {
                    STAGEIN.ret(stack, filename(stack, var.getField(p))[0]);
                }
            }
            catch (MappingDependentException e) {
            	logger.debug(e);
                deperr = true;
                mdeperr = true;
            }
            catch (HandleOpenException e) {
                throw new FutureNotYetAvailable(addFutureListener(stack, e.getSource()));
            }
            catch (InvalidPathException e) {
                throw new ExecutionException(e);
            }
            if (deperr || mdeperr) {
                NamedArguments named = ArgUtil.getNamedReturn(stack); 
                named.add("deperror", deperr);
                named.add("mdeperror", mdeperr);
            }
        }
        else {
            // we still wait until the primitive value is there
            waitFor(var, stack);
        }
        return null;
    }
}
