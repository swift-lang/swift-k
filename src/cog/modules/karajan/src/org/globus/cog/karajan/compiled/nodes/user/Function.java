//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 30, 2012
 */
package org.globus.cog.karajan.compiled.nodes.user;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import k.rt.Frame;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.NamedValue;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.TrackingScope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.compiled.nodes.Node;
import org.globus.cog.karajan.parser.WrapperNode;


public class Function extends UserDefinedFunction {
	private boolean recursive;
	private Map<InvocationWrapper, Scope> lateResolutionWrappers;
	
	public void resolveReferencesLater(InvocationWrapper invocationWrapper, Scope scope) {
		if (lateResolutionWrappers == null) {
			lateResolutionWrappers = new HashMap<InvocationWrapper, Scope>();
		}
		lateResolutionWrappers.put(invocationWrapper, scope);
	}
	
	@Override
	public Node compile(WrapperNode w, Scope scope) throws CompilationException {
		recursive = true;
		try {
			return super.compile(w, scope);
		}
		finally {
			recursive = false;
		}
	}

	@Override
	protected void resolveChannelAndNamedReturns(TrackingScope ts) throws CompilationException {
		if (lateResolutionWrappers != null) {
			for (Map.Entry<InvocationWrapper, Scope> e : lateResolutionWrappers.entrySet()) {
				e.getKey().resolveReferencedChannels(null, e.getValue());
				e.getKey().resolveReferencedParams(null, e.getValue());
			}
			lateResolutionWrappers = null;
		}
		super.resolveChannelAndNamedReturns(ts);
	}

	public boolean isRecursive() {
		return recursive;
	}

	@Override
	protected void _return(Scope scope, Signature sig) {
		Var.Channel ret = scope.parent.lookupChannel("...");
		
		ret.append(new NamedValue(null, null, new Scope.UserDef(this)));
	}

	public void runBody(LWThread thr, List<ChannelRef<Object>> referencedChannels,
			List<VarRef<Object>> referencedNames, int firstIndex, int argCount) {
		if (body == null) {
			return;
		}
		int i = thr.checkSliceAndPopState();
		Stack orig = thr.getStack();
		Stack istk = (Stack) thr.popState();
		try {
			switch(i) {
				case 0:
					istk = initializeArgs(orig, referencedChannels, firstIndex, argCount);
					i++;
				case 1:
					thr.setStack(istk);
					if (CompilerSettings.PERFORMANCE_COUNTERS) {
						startCount++;
					}
					body.run(thr);
					bindParamReturns(orig, istk, referencedNames);
					thr.setStack(orig);
			}
		}
		catch (RuntimeException e) {
			thr.setStack(orig);
			throw e;
		}
		catch (Yield y) {
			thr.setStack(orig);
			y.getState().push(istk);
			y.getState().push(i);
			throw y;
		}
	}
	
	protected Stack initializeArgs(Stack orig, List<ChannelRef<Object>> referencedChannels, int firstIndex, int argCount) {
		Stack istk = defStack.copy();
		istk.enter(this, frameSize);
        bindArgs(orig.top(), istk.top(), firstIndex, argCount);
        bindChannels(orig, istk, referencedChannels);
        return istk;
	}

	protected void bindArgs(Frame src, Frame dst, int firstIndex, int argCount) {
		System.arraycopy(src.getAll(), firstIndex, dst.getAll(), 0, argCount);
		//Arrays.fill(src.getAll(), firstIndex, firstIndex + argCount, null);
	}

	public void bindChannels(Stack parent, Stack def, List<ChannelRef<Object>> wrapperChannels) {
		if (channelReturnRefs != null) {
			Iterator<ChannelRef<Object>> i1 = wrapperChannels.iterator();
			Iterator<ChannelRef<Object>> i2 = channelReturnRefs.iterator();
			
			while (i1.hasNext()) {
				ChannelRef<Object> cr1 = i1.next();
				ChannelRef<Object> cr2 = i2.next();
				//System.out.println(this + ": bind " + cr1 + "(" + cr1.get(parent) + ") -> " + cr2);
				cr2.set(def, cr1.get(parent));
			}
		}
	}
	
	public void bindParamReturns(Stack parent, Stack def, List<VarRef<Object>> wrapperParams) {
		if (namedReturnRefs != null) {
            Iterator<VarRef<Object>> i1 = wrapperParams.iterator();
            Iterator<VarRef<Object>> i2 = namedReturnRefs.iterator();
            
            while (i1.hasNext()) {
            	i1.next().setValue(parent, i2.next().getValue(def));
            }
        }
	}
}
