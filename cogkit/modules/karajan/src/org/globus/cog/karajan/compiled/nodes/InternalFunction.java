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
 * Created on Apr 16, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import k.rt.ExecutionException;
import k.rt.FutureObject;
import k.rt.FutureValue;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgMappingChannel;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.IntrospectionHelper;
import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.ParamWrapperVar;
import org.globus.cog.karajan.analyzer.Pure;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.StaticChannel;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;
import org.globus.cog.karajan.util.Pair;

public abstract class InternalFunction extends Sequential {
	protected static final int DYNAMIC = -1;
	
	private boolean hasVargs;
	
	private List<ChannelRef<?>> channelParams;
	protected ChannelRef<Object> _vargs;
	
	protected abstract Signature getSignature();
	private int firstOptionalIndex = -1, lastOptionalIndex;
		
	protected Param[] params(Object... p) {
		Param[] a = new Param[p.length];
		for (int i = 0; i < p.length; i++) {
			if (p[i] instanceof String) {
				String name = (String) p[i];
				if (name.equals("...")) {
					a[i] = new Param((String) p[i], Param.Type.CHANNEL);
				}
				else {
					a[i] = new Param((String) p[i], Param.Type.POSITIONAL);
				}
			}
			else if (p[i] instanceof Param) {
				a[i] = (Param) p[i];
			}
			else {
				throw new IllegalArgumentException("Unknown parameter object: " + p[i]);
			}
		}
		return a;
	}
	
	protected Param[] returns(Object... p) {
		return params(p);
	}
	
	protected Param channel(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null parameter name");
		}
		return new Param(name, Param.Type.CHANNEL);
	}
	
	protected Param channel(String name, k.rt.Channel<?> value) {
		if (name == null) {
			throw new IllegalArgumentException("Null parameter name");
		}
		return new Param(name, Param.Type.CHANNEL, value);
	}
	
	protected Param channel(String name, int arity) {
		if (name == null) {
			throw new IllegalArgumentException("Null parameter name");
		}
		Param p = new Param(name, Param.Type.CHANNEL);
		if (arity == DYNAMIC) {
			p.setDynamic();
		}
		else {
			p.arity = arity;
		}
		return p;
	}
	
	public static Param optional(String name, Object value) {
		if (name == null) {
			throw new IllegalArgumentException("Null parameter name");
		}
		return new Param(name, Param.Type.OPTIONAL, value);
	}
	
	protected Param identifier(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Null parameter name");
		}
		return new Param(name, Param.Type.IDENTIFIER);
	}
	
	protected Param block(String name) {
		return new Param(name, Param.Type.BLOCK);
	}
	
	protected List<ChannelRef<?>> getChannelParams() {
		return channelParams;
	}
		
	protected void runBody(LWThread thr) {
	}
	
	@Override
	public void run(LWThread thr) {
		int ec = childCount();
        int i = thr.checkSliceAndPopState(ec + 2);
        Stack stack = thr.getStack();
        try {
	        switch (i) {
	        	case 0:
	        		initializeArgs(stack);
	        		i++;
	        	default:
	        			try {
				            for (; i <= ec; i++) {
				            	runChild(i - 1, thr);
				            }
				            checkArgs(stack);
	        			}
	        			catch (IllegalArgumentException e) {
	        				throw new ExecutionException(this, e.getMessage());
	        			}
	        			i = Integer.MAX_VALUE;
	        	case Integer.MAX_VALUE:
			            try {
			            	runBody(thr);
			            }
			            catch (ExecutionException e) {
			            	throw e;
			            }
			            catch (RuntimeException e) {
			            	throw new ExecutionException(this, e);
			            }
		    }
        }
        catch (Yield y) {
            y.getState().push(i, ec + 2);
            throw y;
        }
	}

	protected void initializeArgs(final Stack stack) {
		if (_vargs != null) {
			_vargs.create(stack);
		}
		if (channelParams != null) {
			for (ChannelRef<?> c : channelParams) {
				c.create(stack);
			}
		}
		initializeOptional(stack);
	}
	
	protected void checkArgs(final Stack stack) {
		if (_vargs != null) {
			_vargs.check(stack);
		}
	}
	
	protected void initializeOptional(Stack stack) {
	    if (firstOptionalIndex != -1) {
            Arrays.fill(stack.top().getAll(), firstOptionalIndex, lastOptionalIndex + 1, null);
        }
	}

	protected static class ArgInfo {
		public final LinkedList<WrapperNode> blocks;
		public final Var.Channel vargs;
		public final StaticChannel vargValues;
		public final List<Param> optional;
		public final List<Param> positional;
		public final List<Param> channelParams;
		public final List<Pair<Param, String>> dynamicOptimized;
		public final ParamWrapperVar.IndexRange ir;
		
		public ArgInfo(LinkedList<WrapperNode> blocks, Var.Channel vargs, StaticChannel vargValues, 
				List<Param> optional, List<Param> positional, List<Param> channelParams, 
				List<Pair<Param, String>> dynamicOptimized, 
				ParamWrapperVar.IndexRange ir) {
			this.blocks = blocks;
			this.vargs = vargs;
			this.vargValues = vargValues;
			this.optional = optional;
			this.positional = positional;
			this.channelParams = channelParams;
			this.dynamicOptimized = dynamicOptimized;
			this.ir = ir;
		}
	}
	
	protected ArgInfo compileArgs(WrapperNode w, Signature sig, Scope scope) throws CompilationException {
		resolveChannelReturns(w, sig, scope);
		resolveParamReturns(w, sig, scope);
		
		List<Param> channels = getChannelParams(sig);
		List<Param> optional = getOptionalParams(sig);
		List<Param> positional = getPositionalParams(sig);
		
		// optionals first
		if (optional != null || positional != null) {
			scope.setParams(true);
		}
		
		List<Pair<Param, String>> dynamicOptimized = new LinkedList<Pair<Param, String>>();
		
		addParams(w, sig, scope, channels, optional, positional);
		scanNamed(w, scope, optional, dynamicOptimized);
		optimizePositional(w, scope, positional, dynamicOptimized);
		scanNamed(w, scope, positional, dynamicOptimized);
		scanNotSet(w, scope, optional);
		
		ParamWrapperVar.IndexRange ir = allocateParams(w, sig, scope, channels, optional, positional);
					
		LinkedList<WrapperNode> blocks = checkBlockArgs(w, sig);
		
		Var.Channel vargs = null;
		ArgMappingChannel amc = null;
		if (!positional.isEmpty() || !optional.isEmpty() || hasVargs) {
			if (hasVargs) {
				vargs = scope.lookupChannel("...");
				vargs.setValue(amc = new ArgMappingChannel(w, positional, true));
			}
			else {
				vargs = scope.addChannel("...", amc = new ArgMappingChannel(w, positional, false));
			}
		}

		return new ArgInfo(blocks, vargs, amc, optional, positional, channels, dynamicOptimized, ir);
	}

	protected void scanNotSet(WrapperNode w, Scope scope, List<Param> optional) throws CompilationException {
		if (w.nodeCount() == 0) {
			for (Param p : optional) {
				setArg(w, p, new ArgRef.Static<Object>(p.value));
			}
			optional.clear();
		}
	}

	protected void scanNamed(WrapperNode w, Scope scope, List<Param> params, 
			List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
		Iterator<WrapperNode> i = w.nodes().iterator();
		while (i.hasNext()) {
			WrapperNode c = i.next();
			if (c.getNodeType().equals("k:named")) {
				if (optimizeNamed(w, c, scope, params, dynamicOptimized)) {
					i.remove();
				}
			}
		}
	}

	private boolean optimizeNamed(WrapperNode w, WrapperNode c, Scope scope, List<Param> params, 
			List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
		if (c.nodeCount() == 2) {
			WrapperNode nameNode = c.getNode(0);
			if (nameNode.getNodeType().equals("k:var")) {
				String name = nameNode.getText();
				Param p = getParam(params, name);
				if (p != null) {
					WrapperNode valueNode = c.getNode(1);
					if (setValue(w, scope, p, valueNode, dynamicOptimized)) {
						params.remove(p);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	protected void optimizePositional(WrapperNode w, Scope scope, List<Param> params, 
			List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
		int index = 0;
		Iterator<WrapperNode> i = w.nodes().iterator();
		while (i.hasNext() && index < params.size()) {
			WrapperNode c = i.next();
			Param p = params.get(index);
			if (p.type == Param.Type.IDENTIFIER) {
				return;
			}
			if (setValue(w, scope, p, c, dynamicOptimized)) {
				params.remove(p);
				i.remove();
				index--;
			}
			index++;
		}
	}

	private boolean setValue(WrapperNode w, Scope scope, Param p, WrapperNode c, 
			List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
		if (c.getNodeType().equals("k:var")) {
			VarRef<Object> ref = scope.getVarRef(c.getText());
			if (ref.isStatic()) {
				p.setValue(ref.getValue());
				setArg(w, p, new ArgRef.Static<Object>(ref.getValue()));
				return true;
			}
			else if (ref instanceof VarRef.DynamicLocal) {
				VarRef.DynamicLocal<Object> ref2 = (VarRef.DynamicLocal<Object>) ref;
				setArg(w, p, new ArgRef.Dynamic<Object>(ref2.index));
				dynamicOptimized.add(new Pair<Param, String>(p, c.getText()));
				return true;
			}
			else {
				return false;
			}
		}
		else if (c.getNodeType().equals("k:num")) {
			// there is probably a better way to handle these cases
			// perhaps by compiling children and then allocating entries on the frame
			Object value;
			if (c.getText().indexOf(".") >= 0) {
				value = Double.parseDouble(c.getText());
			}	
			else {
				value = Integer.parseInt(c.getText());
			}
			p.setValue(value);
			setArg(w, p, new ArgRef.Static<Object>(value));
			return true;
		}
		else if (c.getNodeType().equals("k:str")) {
			p.setValue(c.getText());
			setArg(w, p, new ArgRef.Static<Object>(c.getText()));
			return true;
		}
		else {
			return false;
		}
	}

	private Param getParam(List<Param> params, String name) {
		for (Param p : params) {
			if (p.name.equals(name)) {
				return p;
			}
		}
		return null;
	}
	
	protected void addParams(WrapperNode w, Signature sig, Scope scope, List<Param> channels,
			List<Param> optional, List<Param> positional) throws CompilationException {
		processIdentifierArgs(w, sig);
		prepareChannelParams(scope, channels);
		scope.addChannelParams(channels);
		scope.addPositionalParams(positional);
		scope.addOptionalParams(optional);
	}

	protected ParamWrapperVar.IndexRange allocateParams(WrapperNode w, Signature sig, Scope scope, List<Param> channels,
			List<Param> optional, List<Param> positional) throws CompilationException {
		scope.allocatePositionalParams(positional);
		return scope.allocateOptionalParams(optional);
	}

	@Override
	protected final Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {				
		Signature sig = getSignature();
		
		Scope argScope = new Scope(w, scope);
		Scope.Checkpoint chk = argScope.checkpoint();
		
		ArgInfo ai = compileArgs(w, sig, argScope);
		
		Node n = super.compileChildren(w, argScope);
		
		checkStaticOptimizedParams(w, scope, ai.dynamicOptimized);
		
		if (ai.ir != null) {
		    // unused optional parameters
			scope.releaseRange(ai.ir.currentIndex(), ai.ir.lastIndex());
		}
		
		setRefs(w, ai, argScope);
	
		addLocals(scope);
		
		compileBlocks(w, sig, ai.blocks, scope);
		
		Node self = compileBody(w, argScope, scope);
		
		if (self == null && n == null && ai.blocks == null) {
			argScope.close();
			return null;
		}
		
		argScope.close();
		
		if (this instanceof Pure && ai.blocks == null) {
			return self;
		}
		else {
			return this;
		}
	}

	private void checkStaticOptimizedParams(WrapperNode w, Scope scope, List<Pair<Param, String>> dynamicOptimized) throws CompilationException {
		for (Pair<Param, String> p : dynamicOptimized) {
			VarRef<?> ref = scope.getVarRef(p.t);
			if (ref.isStatic()) {
				setArg(w, p.s, new ArgRef.Static<Object>(ref.getValue()));
			}
		}
	}

	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope) throws CompilationException {
		dynamicChannelReturns(w, getSignature(), scope);
		return this;
	}

	@SuppressWarnings("unchecked")
	protected void setRefs(WrapperNode w, ArgInfo ai, Scope scope) throws CompilationException {
		for (Param p : ai.optional) {
			if (p.dynamic) {
				setArg(w, p, new ArgRef.DynamicOptional<Object>(p.index, p.value));
			}
			else {
				setArg(w, p, new ArgRef.Static<Object>(p.value));
				removeOptionalParam(scope, p);
			}
		}
		
		if (ai.ir != null && ai.ir.isUsed()) {
		    firstOptionalIndex = ai.ir.firstIndex();
		    lastOptionalIndex = ai.ir.currentIndex() - 1;
		}
		
		boolean allPosStatic = true;
		boolean staticAfterDynamic = false;
		int firstDynamicIndex = -1, dynamicCount = 0; 
		for (Param p : ai.positional) {
            if (!p.dynamic) {
                setArg(w, p, new ArgRef.Static<Object>(p.value));
                if (!allPosStatic) {
                	staticAfterDynamic = true;
                }
            }
            else {
            	if (staticAfterDynamic) {
            		throw new CompilationException(w, "Cannot use positional arguments ('" 
            				+ p.name + "') after keyword argument");
            	}
            	if (firstDynamicIndex == -1) {
            		firstDynamicIndex = p.index;
            	}
            	allPosStatic = false;
            	dynamicCount++;
                setArg(w, p, new ArgRef.Dynamic<Object>(p.index));
            }
        }
		
		if (hasVargs) {
			boolean allVargsStatic = !ai.vargs.isDynamic();
			if (allPosStatic) {
				_vargs = (ChannelRef<Object>) makeChannelRef(ai.vargs, ai.vargValues);
			}
			else {
				_vargs = makeArgMappingChannel(firstDynamicIndex, dynamicCount, ai.vargs.getIndex());
				int i1 = ai.positional.size() - dynamicCount;
				int i2 = ai.positional.size();
				((ChannelRef.ArgMapping<Object>) _vargs).setNamesP(ai.positional.subList(i1, i2));
			}
			setChannelArg(w, Param.VARGS, _vargs);
        }
        else {
            if (!allPosStatic) {
            	_vargs = makeArgMappingFixedChannel(firstDynamicIndex, dynamicCount, ai.vargs.getIndex());
				int i1 = ai.positional.size() - dynamicCount;
				int i2 = ai.positional.size();
				((ChannelRef.ArgMapping<Object>) _vargs).setNamesP(ai.positional.subList(i1, i2));
            }
            else if (ai.vargs != null) {
            	_vargs = makeInvalidArgChannel(ai.vargs.getIndex());
            }
        }
		
		if (ai.channelParams != null) {
			for (Param p : ai.channelParams) {
				if (!p.name.equals("...")) {
					StaticChannel c = (StaticChannel) p.value;
					ChannelRef<?> cr = makeChannelRef(scope.lookupChannel(p), c);
					if (channelParams == null) {
						channelParams = new LinkedList<ChannelRef<?>>();
					}
					channelParams.add(cr);
					setChannelArg(w, p, cr);
				}
			}
		}
	}

	protected void removeOptionalParam(Scope scope, Param p) {
	    scope.removeVar(p.varName());
	}

	private ChannelRef<Object> makeInvalidArgChannel(int index) {
		return new ChannelRef.InvalidArg(this, "#channel#...", index);
	}

	protected ChannelRef<Object> makeArgMappingFixedChannel(int firstDynamicIndex, int dynamicCount, int vargsIndex) {
		return new ChannelRef.ArgMappingFixed<Object>(firstDynamicIndex, dynamicCount, vargsIndex);
	}

	protected ChannelRef<Object> makeArgMappingChannel(int firstDynamicIndex, int dynamicCount, int vargsIndex) {
		return new ChannelRef.ArgMapping<Object>(firstDynamicIndex, dynamicCount, vargsIndex);
	}

	private ChannelRef<?> makeChannelRef(Var.Channel vc, StaticChannel c) {
		if (vc.isDynamic()) {
			if (c.isEmpty()) {
				return makeDynamicChannel(vc.name, vc.getIndex());
			}
			else {
				return makeMixedChannel(vc.getIndex(), c.getAll());
			}
		}
		else {
			return makeStaticChannel(vc.getIndex(), c.getAll());
		}
	}

	protected ChannelRef<?> makeStaticChannel(int index, List<Object> values) {
		return new ChannelRef.Static<Object>(values);
	}

	protected ChannelRef<?> makeMixedChannel(int index, List<Object> staticValues) {
		return new ChannelRef.Mixed<Object>(index, staticValues);
	}

	protected ChannelRef<?> makeDynamicChannel(String name, int index) {
		return new ChannelRef.Dynamic<Object>(name, index);
	}

	protected void compileBlocks(WrapperNode w, Signature sig, 
			LinkedList<WrapperNode> blocks, Scope scope) throws CompilationException {
		if (blocks != null) {
			ListIterator<Param> li = sig.getParams().listIterator(sig.getParams().size());
			while (li.hasPrevious()) {
				Param r = li.previous();
				if (r.type == Param.Type.BLOCK) {
					setArg(w, r, compileBlock(blocks.removeLast(), scope));
				}
			}
		}
	}

	protected void setArg(WrapperNode w, Param r, Object value) throws CompilationException {
		IntrospectionHelper.setField(w, this, escapeKeywords(r.name), value);
	}
	
	private static final Set<String> JAVA_KEYWORDS;
	
	static {
		JAVA_KEYWORDS = new HashSet<String>();
		JAVA_KEYWORDS.add("static");
		JAVA_KEYWORDS.add("default");
	}
	
	private String escapeKeywords(String name) {
		if (JAVA_KEYWORDS.contains(name)) {
			return "_" + name;
		}
		else {
			return name;
		}
	}

	protected void setChannelArg(WrapperNode w, Param r, Object value) throws CompilationException {
		IntrospectionHelper.setField(w, this, "c_" + r.fieldName(), value);
	}
	
	protected void setChannelReturn(WrapperNode w, Param r, Object value) throws CompilationException {
		IntrospectionHelper.setField(w, this, "cr_" + r.fieldName(), value);
	}
	
	protected void setReturn(WrapperNode w, Param r, Object value) throws CompilationException {
        IntrospectionHelper.setField(w, this, "r_" + r.fieldName(), value);
    }

	private LinkedList<WrapperNode> checkBlockArgs(WrapperNode w, Signature sig) throws CompilationException {
		LinkedList<WrapperNode> l = null;
		ListIterator<Param> li = sig.getParams().listIterator(sig.getParams().size());
		boolean seenNonBlock = false;
		while (li.hasPrevious()) {
			Param r = li.previous();
			if (r.type == Param.Type.BLOCK) {
				if (seenNonBlock) {
					throw new CompilationException(w, "Cannot have normal arguments after block arguments");
				}
				if (l == null) {
					l = new LinkedList<WrapperNode>();
				}
				l.addFirst(w.removeNode(w.nodeCount() - 1));
			}
			else {
				seenNonBlock = true;
			}
		}
		return l;
	}
	
	private List<Param> getChannelParams(Signature sig) {
		return sig.getChannelParams();
	}

	private List<Param> getOptionalParams(Signature sig) {
		List<Param> l = new LinkedList<Param>();
		for (Param p : sig.getParams()) {
			if (p.type == Param.Type.OPTIONAL) {
				l.add(p);
			}
		}
		return l;
	}
	
	private List<Param> getPositionalParams(Signature sig) {
		List<Param> l = new LinkedList<Param>();
		for (Param p : sig.getParams()) {
			if (p.type == Param.Type.POSITIONAL) {
				l.add(p);
			}
		}
		return l;
	}

	protected void processIdentifierArgs(WrapperNode w, Signature sig) throws CompilationException {
		Iterator<Param> i = sig.getParams().iterator();
		boolean found = false;
		while (i.hasNext()) {
			Param p = i.next();
			if (p.type == Param.Type.IDENTIFIER) {
				if (found) {
					throw new CompilationException(w, "Only one identifier parameter allowed");
				}
				WrapperNode in = w.getNode(0);
				if (!"k:var".equals(in.getNodeType())) {
					throw new CompilationException(w, "Expected identifier");
				}
				
				setArg(w, p, in.getText());
				w.removeNode(in);
				i.remove();
			}
		}
	}

	private List<ArgRef<?>> sortParams(WrapperNode w) {
		// optionals first, 
		return null;
	}

	protected void addLocals(Scope scope) {
	}

	protected void prepareChannelParams(Scope scope, List<Param> channels) {
		if (channels != null && !channels.isEmpty()) {
			for (Param p : channels) {
				if (p.name.equals("...")) {
					hasVargs = true;
				}
				if (p.getValue() == null) {
					StaticChannel c = new StaticChannel();
					p.setValue(c);
				}
			}
		}
	}

	protected void resolveChannelReturns(WrapperNode w, Signature sig, Scope scope) throws CompilationException {
		for (Param p : sig.getChannelReturns()) {
			Var.Channel vc = scope.parent.lookupChannel(p, this);
			if (p.dynamic) {
				vc.appendDynamic();
			}
			setChannelReturn(w, p, scope.parent.getChannelRef(vc));
		}
	}
	
	protected void dynamicChannelReturns(WrapperNode w, Signature sig, Scope scope) throws CompilationException {
		for (Param p : sig.getChannelReturns()) {
			Var.Channel vc = scope.parent.lookupChannel(p, this);
			vc.appendDynamic();
		}
	}
	
	protected void resolveParamReturns(WrapperNode w, Signature sig, Scope scope) throws CompilationException {
        for (Param p : sig.getReturns()) {
        	Var v = scope.parent.lookupParam(p, this);
            v.setDynamic();
            setReturn(w, p, scope.parent.getVarRef(v));
        }
    }

	protected Node compileBlock(WrapperNode w, Scope scope) throws CompilationException {
		return w.compile(this, scope);
	}
	
	protected Object unwrap(Object o) {
		if (o instanceof FutureObject) {
			return ((FutureObject) o).getValue();
		}
		else {
			return o;
		}
	}
	
	protected void waitFor(Object o) {
        if (o instanceof FutureValue) {
            ((FutureValue) o).getValue();
        }
    }
}
