/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 11, 2013
 */
package org.griphyn.vdl.karajan.lib;

import java.util.LinkedList;
import java.util.List;

import k.rt.KRunnable;
import k.rt.SingleValueChannel;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.griphyn.vdl.mapping.nodes.NodeFactory;
import org.griphyn.vdl.type.Field;

public class While extends InternalFunction {

    private String name;    
    private Node body;
    private ChannelRef.DynamicSingleValued<Object> c_next;
    
    private VarRef<Object> var;
    
    private List<StaticRefCount> srefs;
    private Tracer tracer;
    private ArgRef<String> refs;
    
    @Override
    protected Signature getSignature() {
        return new Signature(params(identifier("name"), optional("refs", null), block("body")));
    }
    
    protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
            throws CompilationException {
        srefs = StaticRefCount.build(scope, this.refs.getValue());
        tracer = Tracer.getTracer(this);
        return super.compileBody(w, argScope, scope);
    }
    
    @Override
    protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
            Scope scope) throws CompilationException {
        Var v = scope.addVar(name);
        var = scope.getVarRef(v);
        Var.Channel next = scope.addChannel("next");
        c_next = new ChannelRef.DynamicSingleValued<Object>("next", next.getIndex());
        DynamicScope ds = new DynamicScope(w, scope);
        super.compileBlocks(w, sig, blocks, ds);
        ds.close();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void runBody(LWThread thr) {
        if (body == null) {
            return;
        }
        int i = thr.checkSliceAndPopState(2);
        SingleValueChannel<Object> next = (SingleValueChannel<Object>) thr.popState();
        List<RefCount> drefs = (List<RefCount>) thr.popState();
        LWThread thr2 = (LWThread) thr.popState();
        Stack stack = thr.getStack();
        try {
            out:
                while(true) {
                    switch(i) {
                        case 0:
                            drefs = RefCount.build(stack, srefs);
                            var.setValue(stack, NodeFactory.newRoot(Field.GENERIC_INT, 0));
                            c_next.create(stack);
                            RefCount.incRefs(drefs);
                            next = (SingleValueChannel<Object>) c_next.get(stack);
                            if (tracer.isEnabled()) {
                                tracer.trace(thr, unwrap(next));
                            }
                            i++;
                        case 1:
                            thr2 = thr.fork(new KRunnable() {
                                @Override
                                public void run(LWThread thr) {
                                    body.run(thr);
                                }
                            });
                            i++;
                            thr2.start();
                        default:
                            thr2.waitFor();
                            if (next.isEmpty()) {
                                // must do this twice since the closeDataSet calls
                                // inside the iterate won't be called if the iterate 
                                // condition is true
                                RefCount.decRefs(drefs);
                                RefCount.decRefs(drefs);
                                break out;
                            }
                            else {
                                RefCount.incRefs(drefs);
                            }
                            Object val = next.removeFirst();
                            if (tracer.isEnabled()) {
                                tracer.trace(thr, unwrap(next));
                            }
                            var.setValue(stack, val);
                            i = 1;
                    }
                }
        }
        catch (Yield y) {
            y.getState().push(thr2);
            y.getState().push(drefs);
            y.getState().push(next);
            y.getState().push(i, 2);
            throw y;
        }
    }
}
