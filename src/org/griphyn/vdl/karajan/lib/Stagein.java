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

import k.rt.Channel;
import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.karajan.AssertFailedException;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Types;

public class Stagein extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(Stagein.class);

    private ChannelRef<AbstractDataNode> c_vargs;
    private ChannelRef<Object> cr_stagein;
    private VarRef<Boolean> r_deperror;
    private VarRef<Boolean> r_mdeperror;
    
    private Tracer tracer;
    private String procName;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("..."), returns("deperror", "mdeperror", channel("stagein", DYNAMIC)));
    }

    @Override
    public Node compile(WrapperNode w, Scope scope)
            throws CompilationException {
        Node def = getParent().getParent();
        procName = def.getTextualName();
        tracer = Tracer.getTracer(def, "APPCALL");
        return super.compile(w, scope);
    }

    @Override
    public void runBody(LWThread thr) {
        Stack stack = thr.getStack();
        int index = thr.checkSliceAndPopState(256);
        try {
            Channel<AbstractDataNode> vars = c_vargs.get(stack);
            for (; index < vars.size(); index++) {
                handleVar(stack, vars.get(index));
            }
        }
        catch (Yield y) {
            y.getState().push(index, 256);
            throw y;
        }
        catch (AssertFailedException e) { 
            logger.fatal("swift: assert failed: " + e.getMessage());
            throw e;
        }
        catch (DependentException e) {
            ret(stack, NodeFactory.newRoot(getReturnType(), e));
        }
    }

    public void handleVar(Stack stack, AbstractDataNode var) {
        if (!var.isPrimitive()) {
            boolean deperr = false;
            boolean mdeperr = false;
            try {
                // no need to wait since the app now waits
                // waitForAll(this, var);
                
                if (!Types.EXTERNAL.equals(var.getType())) {
                    cr_stagein.append(stack, var);
                }
            }
            catch (ConditionalYield f) {
                if (tracer.isEnabled()) {
                    tracer.trace(LWThread.currentThread(), procName + " WAIT " + Tracer.getFutureName(f.getFuture()));
                }
                throw f;
            }
            catch (MappingDependentException e) {
                if (logger.isDebugEnabled()) {
            	    logger.debug(e);
                }
                deperr = true;
                mdeperr = true;
            }
            catch (DependentException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e);
                }
                deperr = true;
            }
            catch (Exception e) {
                throw new ExecutionException(this, e);
            }
            if (deperr) {
                this.r_deperror.setValue(stack, true);
            }
            if (mdeperr) {
                this.r_mdeperror.setValue(stack, true);
            }
        }
    }

    @Override
    public Object function(Stack stack) {
        // not used
        return null;
    }
}
