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
 */
package org.globus.cog.karajan.compiled.nodes.user;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.SingleValueChannel;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.ContainerScope;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Param.Type;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.CompoundNode;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;

public abstract class UserDefinedFunction extends CompoundNode {
	private int callcount;
	private Signature sig;
	protected Node body;
	
	private Map<String, List<Node>> channelReturns;
	private Map<String, List<Node>> paramReturns;
	protected List<ChannelRef<Object>> channelReturnRefs;
	protected List<VarRef<Object>> namedReturnRefs;
	protected List<ArgRef.RuntimeOptional<Object>> runtimeOptionalValues;
	
	protected Stack defStack;
	protected int frameSize, optVarChannelIndex;
	
	@Override
	public void run(LWThread thr) {
		if (runtimeOptionalValues != null) {
			int i = thr.checkSliceAndPopState(runtimeOptionalValues.size() + 2);
			@SuppressWarnings("unchecked")
			SingleValueChannel<Object> c = (SingleValueChannel<Object>) thr.popState();
			Stack stack = thr.getStack();
			try {
				for (; i <= runtimeOptionalValues.size(); i++) {
					if (i == 0) {
						c = new SingleValueChannel<Object>();
						stack.top().set(optVarChannelIndex, c);
					}
					else {
						ArgRef.RuntimeOptional<Object> p = runtimeOptionalValues.get(i - 1);
						if (CompilerSettings.PERFORMANCE_COUNTERS) {
							startCount++;
						}
						p.getNode().run(thr);
						if (c.size() != 1) {
							throw new ExecutionException(this, "More than one value for parameter '" + p.getName() + "'");
						}
						p.setValue(c.get(0));
						c.removeFirst();
					}
				}
			}
			catch (Yield y) {
				y.getState().push(c);
				y.getState().push(i, runtimeOptionalValues.size() + 2);
				throw y;
			}
		}
		this.defStack = thr.getStack().copy();
	}

	public Signature getSignature() {
		return sig;
	}
	
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		Scope cs = new Scope(w, scope);
		Integer l = (Integer) WrapperNode.getTreeProperty(WrapperNode.LINE, w);
		if (l != null) {
			setLine(l);
		}
		setType(w.getNodeType());
		Node fn = compileChildren(w, cs);
		cs.close();
		return this;
	}

	@Override
	public Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		List<Param> params = getParams(w, scope);

		compileOptionalParamValues(params, scope);
		
		sig = new Signature(params);
		
		_return(scope, sig);
		
		ContainerScope cs = new ContainerScope(w, scope);
		TrackingScope ts = new TrackingScope(cs);
		ts.setAllowChannelReturns(false);
		ts.setAllowNamedReturns(false);
		ts.setFixedAllocation(true);
		
		Scope bodyScope = new Scope(ts);
		
		addParams(bodyScope, sig.getChannelParams(), sig.getParams());
		
		body = w.getNode(w.nodeCount() - 1).compile(this, bodyScope);
		if (body != null) {
			body.setParent(this);
		}
		
		channelReturns = ts.getReferencedChannels();
		paramReturns = ts.getReferencedParams();
		
		resolveChannelAndNamedReturns(ts);
		
		frameSize = cs.size();
		
		return null;
	}
	
	protected void resolveChannelAndNamedReturns(TrackingScope ts) throws CompilationException {
		resolveChannelReturns(channelReturns, ts);
		resolveNamedReturns(paramReturns, ts);
	}

	private void compileOptionalParamValues(List<Param> params, Scope scope) throws CompilationException {
		Scope ns = new Scope(scope);
		Var.Channel values = ns.addChannel("...");
		this.optVarChannelIndex = values.getIndex();
		for (Param p : params) {
			if (p.type == Param.Type.OPTIONAL) {
				values.clear();
				WrapperNode wn = (WrapperNode) p.value;
				Node compiled = compileChild(wn, ns);
				if (values.getAll().size() > 1) {
					throw new CompilationException(wn, "An optional argument can only have one value");
				}
				if (values.isDynamic()) {
					p.setValue(compiled);
					if (runtimeOptionalValues == null) {
						runtimeOptionalValues = new LinkedList<ArgRef.RuntimeOptional<Object>>();
					}
					runtimeOptionalValues.add(new ArgRef.RuntimeOptional<Object>(p.index, p.name, compiled));
				}
				else {
					p.setValue(values.getAll().get(0));
				}
			}
		}
		ns.close();
	}

	protected abstract void _return(Scope scope, Signature sig);

	private void resolveChannelReturns(Map<String, List<Node>> crs, TrackingScope bodyScope) {
		if (crs != null) {
			channelReturnRefs = new LinkedList<ChannelRef<Object>>();
			for (String c : crs.keySet()) {
				Var.Channel vc = bodyScope.lookupChannel(c);
				//System.out.println(this + " -> " + c + " -> " + vc.getIndex());
				channelReturnRefs.add(new ChannelRef.Dynamic<Object>(vc.name, vc.getIndex()));
			}
		}
	}
	
	private void resolveNamedReturns(Map<String, List<Node>> nrs, TrackingScope bodyScope) {
		if (nrs != null) {
			namedReturnRefs = new LinkedList<VarRef<Object>>();
			for (String n : nrs.keySet()) {
				Var v = bodyScope.lookupParam(n);
				namedReturnRefs.add(bodyScope.getVarRef(v));
			}
		}
	}

	private void addParams(Scope bodyScope, List<Param> channelParams, List<Param> params) {
		for (Param p : channelParams) {
			bodyScope.addVar(p.name);
		}
		
		for (Param p : params) {
			if (p.type == Param.Type.POSITIONAL) {
				bodyScope.addVar(p.name);
			}
		}
		
		for (Param p : params) {
            if (p.type == Param.Type.OPTIONAL) {
                bodyScope.addVar(p.name);
            }
        }
	}

	private String channelNameToParamName(String name) {
		if (name.equals("#channel#-")) {
			return "...";
		}
		else {
			return name.substring("#channel#".length());
		}
	}

	protected List<Param> getParams(WrapperNode w, Scope scope) throws CompilationException {
		
		Var.Channel args = scope.addChannel("...");
		
		for (int i = 0; i < w.nodeCount() - 1; i++) {
			WrapperNode c = w.getNode(i);
			if (c.getNodeType().equals("k:var")) {
				String name = c.getText();
				if (name.equals("...")) {
					args.append(new Param("...", Param.Type.CHANNEL));
				}
				else {
					args.append(new Param(c.getText(), Param.Type.POSITIONAL));
				}
			}
			else if (c.getNodeType().equals("k:named")) {
				Param opt = new Param(c.getNode(0).getText(), Param.Type.OPTIONAL);
				opt.setValue(c.getNode(1));
				args.append(opt);
			}
			else {
				compileChild(c, scope);
			}
		}
		
		List<Param> params = new LinkedList<Param>();
		for (Object o : args.getAll()) {
			if (o instanceof Param) {
				params.add((Param) o);
			}
			else {
				throw new CompilationException(w, "Unknown parameter type: " + o);
			}
		}
		
		setIndices(params);
		return params;
	}
	
	private void setIndices(List<Param> params) {
		int index = 0;
		index = setIndices(params, Param.Type.CHANNEL, index);
		index = setIndices(params, Param.Type.POSITIONAL, index);
		index = setIndices(params, Param.Type.OPTIONAL, index);
	}

	private int setIndices(List<Param> params, Type type, int index) {
		for (Param p : params) {
			if (p.type == type) {
				p.setIndex(index++);
			}
		}
		return index;
	}

	public Map<String, List<Node>> getChannelReturns() {
		return channelReturns;
	}

	public Map<String, List<Node>> getParamReturns() {
		return paramReturns;
	}
	
	@Override
    public void dump(PrintStream ps, int level) throws IOException {
        super.dump(ps, level);
        if (body != null) {
            body.dump(ps, level + 1);
        }
    }

	protected List<ArgRef.RuntimeOptional<Object>> getRuntimeOptionalValues() {
		return runtimeOptionalValues;
	}
}