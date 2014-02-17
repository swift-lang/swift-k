// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.NamedValue;
import org.globus.cog.karajan.analyzer.Pure;
import org.globus.cog.karajan.analyzer.RecursiveFunctionChannel;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Scope.Def;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.Var.Channel;
import org.globus.cog.karajan.parser.WrapperNode;

public class SetVarK extends CompoundNode {
	private int startIndex, endIndex;
	private ChannelRef.ArgMapping<Object> values;

	@Override
	public void run(LWThread thr) {
        int i = thr.checkSliceAndPopState();
        Stack stack = thr.getStack();
        try {
	        switch (i) {
	        	case 0:
	        		if (startIndex >= 0) {
	        			values.create(stack);
	        		}
	        		i++;
	        	default:
	        		int nc = childCount();
	        		for (; i <= nc; i++) {
	        			runChild(i - 1, thr);
	        		}
	        		values.check(stack);
		    }
        }
        catch (Exception e) {
        	throw new ExecutionException(this, e);
        }
        catch (Yield y) {
            y.getState().push(i);
            throw y;
        }
	}

	@Override
	protected Node compileChildren(WrapperNode wn, Scope scope) throws CompilationException {
		LinkedList<String> vars = getIdents(wn, scope);
		int initialSize = vars.size();
		startIndex = scope.parent.reserveRange(initialSize);
		
		Var.Channel svalues = scope.addChannel("...");
		//System.out.println(wn + " ... at " + svalues.getIndex());
		
		if (wn.getNode(1).getNodeType().equals("function")) {
			if (vars.size() != 1) {
				throw new CompilationException(wn, "Expected a single identifier");
			}
			svalues.setValue(new RecursiveFunctionChannel(wn, scope.parent, vars.get(0)));
		}
		
		Node cn = wn.getNode(1).compile(this, scope);
		
		if (svalues.size() > vars.size()) {
			throw new CompilationException(wn, "Spurious values in assignment");
		}
		if (!svalues.isDynamic() && (svalues.size() < vars.size())) {
			throw new CompilationException(wn, "Insufficient values in assignment");
		}
		
		addStatic(vars, svalues, scope);
		
		scope.parent.releaseRange(startIndex + vars.size(), startIndex + initialSize - 1);
		
		if (vars.isEmpty()) {
			startIndex = -1;
			if (cn instanceof Pure) {
				return null;
			}
			else {
				return cn;
			}
		}
		
		int index = startIndex;
		for (String name : vars) {
			Var v = new Var(name);
			v.setDynamic();
			scope.parent.addVar(v, index++);
		}
		
		values = new ChannelRef.ArgMappingFixed<Object>(startIndex, vars.size(), svalues.getIndex());
		values.setNamesS(vars);
		addChild(cn);
		return this;
	}

	private void addStatic(List<String> vars, Channel svalues, Scope scope) {
		Iterator<String> i1 = vars.iterator();
		Iterator<Object> i2 = svalues.getAll().iterator();
		while (i2.hasNext()) {
			Object value = i2.next();
			String name = i1.next();
			if (value instanceof NamedValue) {
				// function
				NamedValue nv = (NamedValue) value;
				scope.parent.addDef(nv.ns, name, (Def) nv.value);
			}
			else if (value != null) {
				scope.parent.addVar(name, value);
			}
			else {
				return;
			}
			i1.remove();
			i2.remove();
		}
	}

	private LinkedList<String> getIdents(WrapperNode wn, Scope scope) throws CompilationException {
		if (wn.nodeCount() < 2) {
			throw new CompilationException(wn, "Expected indentifier(s)");
		}
		LinkedList<String> l = new LinkedList<String>();
		WrapperNode cn = wn.getNode(0);
		if (cn.getNodeType().equals("k:var")) {
			l.add(cn.getText());
		}
		else if (cn.getNodeType().equals("k:sequential")) {
			for (WrapperNode ccn : cn.nodes()) {
				if (ccn.getNodeType().equals("k:var")) {
					l.add(ccn.getText());
				}
				else {
					throw new CompilationException(ccn, "Expected identifier");
				}
			}
		}
		else {
			throw new CompilationException(cn, "Expected identifier");
		}
		return l;
	}
}