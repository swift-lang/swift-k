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

import k.rt.ConditionalYield;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.futures.FutureFault;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.DependentException;
import org.griphyn.vdl.mapping.MappingDependentException;
import org.griphyn.vdl.mapping.Path;
import org.griphyn.vdl.mapping.nodes.AbstractDataNode;

public class Stagein extends SwiftFunction {
    public static final Logger logger = Logger.getLogger(Stagein.class);

    private ArgRef<AbstractDataNode> var;
    private ChannelRef<Object> cr_stagein;
    private VarRef<Boolean> r_deperror;
    private VarRef<Boolean> r_mdeperror;
    
    private Tracer tracer; 
    private String procName;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params("var"), returns("deperror", "mdeperror", channel("stagein", DYNAMIC)));
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
    public Object function(Stack stack) {
        AbstractDataNode var = this.var.getValue(stack);
        if (!var.isPrimitive()) {
            boolean deperr = false;
            boolean mdeperr = false;
            try {
                Collection<Path> fp = var.getFringePaths();
                try {
                    for (Path p : fp) {
                        AbstractDataNode n = (AbstractDataNode) var.getField(p);
                    	n.waitFor(this);
                    }
                }
                catch (DependentException e) {
                    deperr = true;
                }
                
                cr_stagein.append(stack, var);
            }
            catch (ConditionalYield f) {
                if (tracer.isEnabled()) {
                    tracer.trace(LWThread.currentThread(), procName + " WAIT " + Tracer.getFutureName(f.getFuture()));
                }
                throw f;
            }
            catch (MappingDependentException e) {
            	logger.debug(e);
                deperr = true;
                mdeperr = true;
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
        else {
            // we still wait until the primitive value is there
            if (tracer.isEnabled()) {
                try {
                    var.waitFor(this);
                }
                catch (FutureFault f) {
                    tracer.trace(LWThread.currentThread(), procName + " WAIT " + Tracer.getFutureName(f.getFuture()));
                    throw f;
                }
            }
            else {
                var.waitFor(this);
            }
        }
        return null;
    }
}
