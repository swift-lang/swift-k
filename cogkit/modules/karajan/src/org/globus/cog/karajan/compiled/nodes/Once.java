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

// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.LinkedList;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.Cache;

public class Once extends InternalFunction {
	private ArgRef<Object> on;
    private Node body;
    
    public static final String KEY = "#cachekey";

    private ChannelRef<Object> c_vargs;
    private ChannelRef<Object> cr_vargs;
    
    private Cache cache = new Cache();

    @Override
    protected Signature getSignature() {
        return new Signature(params("on", block("body")), returns(channel("...")));
    }
    
	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
	    Var.Channel vargs = scope.addChannel("...");
        c_vargs = new ChannelRef.Dynamic<Object>("...", vargs.getIndex());
        vargs.appendDynamic();
		super.compileBlocks(w, sig, blocks, scope);
	}

	@Override
    protected void runBody(LWThread thr) {
        int i = thr.checkSliceAndPopState(2);
        int frame = thr.popIntState();
        Object key = thr.popState();
        Stack stack = thr.getStack();
        try {
            switch (i) {
                case 0:
                    frame = stack.frameCount();
                    key = this.on.getValue(stack);
                    i++;
                case 1:
                    boolean shouldContinue = checkCached(stack, key);
                    if (!shouldContinue) {
                        break;
                    }
                    c_vargs.create(stack);
                    i++;
                default:
                    if (CompilerSettings.PERFORMANCE_COUNTERS) {
                        startCount++;
                    }
                    body.run(thr);
                    setValue(key, stack);
            }
        }
        catch (Yield y) {
            y.getState().push(key);
            y.getState().push(frame);
            y.getState().push(i, 2);
            throw y;
        }
        catch (ExecutionException e) {
            stack.dropToFrame(frame);
            setException(key, thr.getStack(), e);
            throw e;
        }
    }

    protected boolean checkCached(Stack stack, Object key) {
        return initialize(key, stack);
    }

    protected boolean initialize(Object key, Stack stack) {
        FutureObject fv;
        boolean initial = false;
        synchronized (cache) {
            fv = (FutureObject) cache.getCachedValue(key);
            if (fv == null) {
                fv = new FutureObject();
                cache.addAndLock(key, fv);
                initial = true;
            }
        }
        if (initial) {
            return true;
        }
        else {
            Returns r = (Returns) fv.getValue();
            returnCachedValues(stack, r);
            return false;
        }
    }

    public void setValue(Object key, Stack stack) throws ExecutionException {
        Returns ret = getReturnValues(stack);
        returnCachedValues(stack, ret);
        synchronized (cache) {
            FutureObject f = (FutureObject) cache.getCachedValue(key);
            f.setValue(ret);
            cache.unlock(key);
        }
    }

    public void setException(Object key, Stack stack, RuntimeException e) {
        synchronized (cache) {
            FutureObject f = (FutureObject) cache.getCachedValue(key);
            f.fail(e);
        }
    }

    private Returns getReturnValues(Stack stack) {
        Returns r = new Returns();
        r.args.addAll(c_vargs.get(stack).getAll());
        return r;
    }

    private void returnCachedValues(Stack stack, Returns ret) {
        cr_vargs.get(stack).addAll(ret.args);
    }
    
    private static class Returns {
        public final List<Object> args;
        
        public Returns() {
            args = new LinkedList<Object>();
        }
    }
}
