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


package org.griphyn.vdl.karajan.lib;

import java.util.ArrayList;
import java.util.List;

import k.rt.Stack;
import k.thr.LWThread;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class PartialCleanOrClose<T> extends Node {
	public static final Logger logger = Logger.getLogger(CloseDataset.class);
	
	private static class Entry<S> {
	    public final VarRef<S> ref;
	    public final int count;
	    
	    public Entry(VarRef<S> ref, int count) {
	        this.ref = ref;
	        this.count = count;
	    }
	}
	
	private List<Entry<T>> l;
	
	@Override
    public void run(LWThread thr) {
        Stack stack = thr.getStack();
        for (Entry<T> e : l) {
            doWhatNeedsToBeDone(e.ref.getValue(stack), e.count);
        }
    }
	
    protected abstract void doWhatNeedsToBeDone(T var, int count);

    @Override
    public Node compile(WrapperNode wn, Scope scope) throws CompilationException {
        super.compile(wn, scope);
        l = new ArrayList<Entry<T>>();
        boolean flip = true;
        VarRef<T> ref = null;
        for (WrapperNode c : wn.nodes()) {
            if (flip) {
                String name = c.getText();
                ref = scope.getVarRef(name);
            }
            else {
                int count = Integer.parseInt(c.getText());
                if (ref.isStatic() && ignoreStaticRefs()) {
                    // do nothing
                }
                else {
                    // don't touch statically optimized things
                    l.add(new Entry<T>(ref, count));
                }
            }
            flip = !flip;
        }
        
        if (l.isEmpty()) {
            return null;
        }
        else {
            return this;
        }
    }

    protected abstract boolean ignoreStaticRefs();
}
