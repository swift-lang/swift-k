/*
 * Copyright 2012 University of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
 * Created on Jan 5, 2007
 */
package org.griphyn.vdl.karajan.lib;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.Arg.Channel;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.NamedArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.futures.FutureFault;
import org.globus.cog.karajan.workflow.nodes.FlowNode;
import org.griphyn.vdl.engine.Karajan;
import org.griphyn.vdl.mapping.AbstractDataNode;
import org.griphyn.vdl.mapping.DSHandle;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;

public class Stagein extends VDLFunction {
    public static final Logger logger = Logger.getLogger(Stagein.class);

    public static final Arg VAR = new Arg.Positional("var");
    
    public static final Channel STAGEIN = new Channel("stagein");
    
    private Tracer tracer; 
    private String procName;

    static {
        setArguments(Stagein.class, new Arg[] { VAR });
    }
    
    @Override
    protected void initializeStatic() {
        super.initializeStatic();
        FlowNode def = (FlowNode) getParent().getParent();
        procName = Karajan.demangle(def.getTextualName());
        tracer = Tracer.getTracer(def, "APPCALL");
    }

    private boolean isPrimitive(DSHandle var) {
        return (var instanceof AbstractDataNode && ((AbstractDataNode) var)
            .isPrimitive());
    }

    protected Object function(VariableStack stack) throws ExecutionException {
        AbstractDataNode var = (AbstractDataNode) VAR.getValue(stack);
        if (!isPrimitive(var)) {
            boolean deperr = false;
            boolean mdeperr = false;
            try {
                Collection<Path> fp = var.getFringePaths();
                try {
                    for (Path p : fp) {
                        AbstractDataNode n = (AbstractDataNode) var.getField(p);
                    	n.waitFor();
                    	if (tracer.isEnabled()) {
                    	    tracer.trace(stack, procName + " available " + Tracer.getVarName(n));
                    	}
                    }
                }
                catch (DependentException e) {
                    deperr = true;
                }
                for (Path p : fp) {
                    STAGEIN.ret(stack, filename(stack, var.getField(p))[0]);
                }
            }
            catch (FutureFault f) {
                if (tracer.isEnabled()) {
                    tracer.trace(stack, procName + " wait " + Tracer.getFutureName(f.getFuture()));
                }
                throw f;
            }
            catch (MappingDependentException e) {
            	logger.debug(e);
                deperr = true;
                mdeperr = true;
            }
            catch (Exception e) {
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
            if (tracer.isEnabled()) {
                try {
                    var.waitFor();
                }
                catch (FutureFault f) {
                    tracer.trace(stack, procName + " waiting for " + Tracer.getFutureName(f.getFuture()));
                    throw f;
                }
            }
            else {
                var.waitFor();
            }
        }
        return null;
    }
}
