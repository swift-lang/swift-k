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
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.compiled.nodes.user;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.MemoryChannel;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.ParamWrapperVar;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.Pair;

public class InvocationWrapper extends InternalFunction {
	
	private final Function fn;
	
	private List<ChannelRef<Object>> referencedChannels;
	private List<VarRef<Object>> referencedNames;
	private Object[] staticArgs;
	private int firstIndex, argCount;

	public InvocationWrapper(Function fn) {
		this.fn = fn;
	}

	@Override
	protected Signature getSignature() {
		return fn.getSignature().copy();
	}
	
	@Override
    public void run(LWThread thr) {
        int ec = childCount();
        int i = thr.checkSliceAndPopState(ec + 1);
        Stack stack = thr.getStack(); 
        try {
            switch (i) {
                case 0:
                    initializeArgs(stack);
                    i++;
                default:
                        for (; i <= ec; i++) {
                            runChild(i - 1, thr);
                        }
                        try {
                            runBody(thr);
                        }
                        catch (ExecutionException e) {
                        	e.push(this);
                            throw e;
                        }
                        catch (RuntimeException e) {
                            throw new ExecutionException(this, e);
                        }
            }
        }
        catch (Yield y) {
            y.getState().addTraceElement(this);
            y.getState().push(i, ec + 1);
            throw y;
        }
    }

	@Override
	protected void addParams(WrapperNode w, Signature sig, Scope scope, List<Param> channels,
			List<Param> optional, List<Param> positional) throws CompilationException {
	}

	@Override
	protected ParamWrapperVar.IndexRange allocateParams(WrapperNode w, Signature sig, Scope scope, List<Param> channels,
			List<Param> optional, List<Param> positional) throws CompilationException {
		prepareChannelParams(scope, channels);
		
		firstIndex = scope.addParams(channels, optional, positional, this);
		argCount = channels.size() + optional.size() + positional.size();
		return null;
	}
	
	@Override
	protected void scanNotSet(WrapperNode w, Scope scope, List<Param> optional)
			throws CompilationException {
	}

	@Override
	protected void scanNamed(WrapperNode w, Scope scope, List<Param> params,
			List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
	}

	@Override
	protected void optimizePositional(WrapperNode w, Scope scope, List<Param> params,
			List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setArg(WrapperNode w, Param r, Object value) throws CompilationException {
		if (value instanceof ArgRef.Static) {
			setStaticArg(r.index, ((ArgRef.Static<Object>) value).getValue());
		}
		else if (value instanceof ArgRef.DynamicOptional) {
			setStaticArg(r.index, ((ArgRef.DynamicOptional<Object>) value).getValue());
		}
	}

	private void setStaticArg(int index, Object value) {
	    index -= firstIndex;
		if (staticArgs == null) {
			staticArgs = new Object[index + 1];
		}
		else if (staticArgs.length <= index) {
			Object[] nsa = new Object[index + 1];
			System.arraycopy(staticArgs, 0, nsa, 0, staticArgs.length);
			staticArgs = nsa;
		}
		staticArgs[index] = value;
	}

	@Override
	protected void removeOptionalParam(Scope scope, Param p) {
		// not done here since static optional params are still
	    // created on the stack
	}

	@Override
	protected void setChannelArg(WrapperNode w, Param r, Object value) throws CompilationException {
	}

	@Override
	protected void setChannelReturn(WrapperNode w, Param r, Object value) throws CompilationException {
	}

	
	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		if (fn.isRecursive()) {
			fn.resolveReferencesLater(this, scope);
		}
		else {
			resolveReferencedChannels(w, scope);
			resolveReferencedParams(w, scope);
		}
		return this;
	}


	protected void resolveReferencedParams(WrapperNode w, Scope scope) throws CompilationException {
		Map<String, List<Node>> nrs = fn.getParamReturns();
		if (nrs != null) {
			referencedNames = new LinkedList<VarRef<Object>>();
			for (String nr : nrs.keySet()) {
				try {
					Var v = scope.lookupParam(nr, this);
					v.setDynamic();
					referencedNames.add(new VarRef.DynamicLocal<Object>(nr, v.getIndex()));
				}
				catch (IllegalArgumentException e) {
					throw new CompilationException(w, "Parameter '" + nr + "' not found. Referenced by:\n" 
							+ getParamReferenceChain(nr, nrs.get(nr), 0, new StringBuilder()));
				}
			}
		}
	}

	protected void resolveReferencedChannels(WrapperNode w, Scope scope) throws CompilationException {
		Map<String, List<Node>> crs = fn.getChannelReturns();
		if (crs != null) {
			referencedChannels = new LinkedList<ChannelRef<Object>>();
			for (String cr : crs.keySet()) {
				try {
					Var.Channel c = scope.lookupChannel(cr, this);
					c.appendDynamic();
					referencedChannels.add(scope.getChannelRef(c));
				}
				catch (IllegalArgumentException e) {
					throw new CompilationException(w, "Channel '" + cr + "' not found. Referenced by:\n" 
							+ getChannelReferenceChain(cr, crs.get(cr), 0, new StringBuilder()));
				}
			}
		}
	}
	
	private StringBuilder getChannelReferenceChain(String name, List<Node> l, int depth, StringBuilder sb) {
		for (Node n : l) {
			for (int i = 0; i <= depth; i++) {
				sb.append("\t");
			}
			sb.append(n.toString());
			sb.append('\n');
			if (n instanceof InvocationWrapper) {
				List<Node> ll = ((InvocationWrapper) n).fn.getChannelReturns().get(name);
				if (ll != null) {
					getChannelReferenceChain(name, ll, depth + 1, sb);
				}
			}
		}
		return sb;
	}
	
	private StringBuilder getParamReferenceChain(String name, List<Node> l, int depth, StringBuilder sb) {
		for (Node n : l) {
			for (int i = 0; i <= depth; i++) {
				sb.append("\t");
			}
			sb.append(n.toString());
			sb.append('\n');
			if (n instanceof InvocationWrapper) {
				List<Node> ll = ((InvocationWrapper) n).fn.getParamReturns().get(name);
				if (ll != null) {
					getParamReferenceChain(name, ll, depth + 1, sb);
				}
			}
		}
		return sb;
	}

	@Override
	protected ChannelRef<?> makeStaticChannel(int index, List<Object> values) {
		return new ChannelRef.DynamicPreset<Object>(index, new MemoryChannel<Object>(values));
	}

	@Override
	protected void initializeArgs(Stack stack) {
	    if (staticArgs != null) {
            System.arraycopy(staticArgs, 0, stack.top().getAll(), firstIndex, staticArgs.length);
        }
		super.initializeArgs(stack);
	}
	
	

	@Override
	protected void initializeOptional(Stack stack) {
		List<ArgRef.RuntimeOptional<Object>> opt = fn.getRuntimeOptionalValues();
        if (opt != null) {
            for (ArgRef.RuntimeOptional<Object> o : opt) {
                o.set(stack.top(), firstIndex);
            }
        }
	}

	@Override
	protected void runBody(LWThread thr) {
		fn.runBody(thr, referencedChannels, referencedNames, firstIndex, argCount);
	}

	public void updateReferenced(Function fn) {
	}
}