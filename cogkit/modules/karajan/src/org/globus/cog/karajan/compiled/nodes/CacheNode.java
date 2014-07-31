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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.util.Cache;

public class CacheNode extends BufferedInternalFunction {
	private ArgRef<Object> on;
	private Node body;
	
	public static final String KEY = "#cachekey";
	public static final String CACHE = "#cache#cache";
	
	private VarRef<Object> key;
	protected VarRef<Context> context;

	@Override
	protected Signature getSignature() {
		return new Signature(params("on", block("body")));
	}
	
	@Override
	protected void addLocals(Scope scope) {
		super.addLocals(scope);
		Var vkey = scope.addVar(KEY);
		key = scope.getVarRef(vkey);
		
		context = scope.getVarRef("#context");
	}

	@Override
	protected void runBody(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		int frame = thr.popIntState();
		Stack stack = thr.getStack();
		try {
			switch (i) {
				case 0:
					frame = stack.frameCount();
					i++;
				case 1:
					// this sets "key", which is local
					boolean shouldContinue = checkCached(stack);
					if (!shouldContinue) {
						break;
					}
					i++;
				case 2:
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					// this uses "key", set above
					setValue(stack);
			}
		}
		catch (Yield y) {
			y.getState().push(frame);
			y.getState().push(i);
			throw y;
		}
		catch (ExecutionException e) {
			stack.dropToFrame(frame);
			setException(thr.getStack(), e);
			throw e;
		}
	}

	protected boolean checkCached(Stack stack) {
		Object on = this.on.getValue(stack);
		if (on == null) {
			on = this;
		}
		key.setValue(stack, on);
		return initialize(on, stack);
	}

	protected boolean initialize(Object key, Stack stack) {
		Cache cache = getCache(stack, key == this);
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
			addBuffers(stack);
			return true;
		}
		else {
			Returns r = (Returns) fv.getValue();
			returnCachedValues(stack, r);
			return false;
		}
	}

	public void setValue(Stack stack) throws ExecutionException {
		Returns ret = getReturnValues(stack);
		returnCachedValues(stack, ret);
		Object key = this.key.getValue(stack);
		Cache cache = getCache(stack, key == this);
		synchronized (cache) {
			FutureObject f = (FutureObject) cache.getCachedValue(key);
			f.setValue(ret);
			cache.unlock(key);
		}
	}

	public void setException(Stack stack, RuntimeException e) {
		Object key = this.key.getValue(stack);
        Cache cache = getCache(stack, key == this);
		synchronized (cache) {
            FutureObject f = (FutureObject) cache.getCachedValue(key);
            f.fail(e);
		}
	}

	private Returns getReturnValues(Stack stack) {
		Returns r = new Returns();
		if (getChannelBuffers() != null) {
			for (ChannelRef.Buffer<Object> c : getChannelBuffers()) {
				r.channels.add(c.get(stack));
			}
		}
		if (getArgBuffers() != null) {
			for (VarRef.Buffer<Object> a : getArgBuffers()) {
				r.args.add(a.getValue(stack));
			}
		}
		return r;
	}

	private void returnCachedValues(Stack stack, Returns ret) {
		if (getChannelBuffers() != null) {
			Iterator<ChannelRef.Buffer<Object>> i1 = getChannelBuffers().iterator();
			Iterator<k.rt.Channel<Object>> i2 = ret.channels.iterator();
			while (i1.hasNext()) {
				i1.next().commit(stack, i2.next());
			}
		}
		
		if (getArgBuffers() != null) {
			Iterator<VarRef.Buffer<Object>> i1 = getArgBuffers().iterator();
			Iterator<Object> i2 = ret.args.iterator();
			while (i1.hasNext()) {
				i1.next().setValue(stack, i2.next());
			}
		}
	}

	private static final Cache scache = new Cache();

	protected Cache getCache(Stack stack, boolean staticdef) throws ExecutionException {
		if (staticdef) {
			return scache;
		}
		else {
			Context ctx = this.context.getValue(stack);
			synchronized(ctx) {
				Cache c = (Cache) ctx.getAttribute(CACHE);
				if (c == null) {
					c = new Cache();
					ctx.setAttribute(CACHE, c);
				}
				return c;
			}
		}
	}
	
	private static class Returns {
		public final List<k.rt.Channel<Object>> channels;
		public final List<Object> args;
		
		public Returns() {
			channels = new LinkedList<k.rt.Channel<Object>>();
			args = new LinkedList<Object>();
		}
	}
}
