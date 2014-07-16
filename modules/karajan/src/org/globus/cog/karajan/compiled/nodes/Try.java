// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Oct 6, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;

public class Try extends BufferedNode {	
	public static final String EXCEPTION_VAR_NAME = "#exception";
	private VarRef<ExecutionException> exception;	

	@Override
	protected Node compileChildren(WrapperNode w, Scope scope) throws CompilationException {
		Var v = scope.addVar(EXCEPTION_VAR_NAME);
		exception = scope.getVarRef(v);
		DynamicScope cs = new DynamicScope(w, scope);
		Node fn = super.compileChildren(w, cs);
		cs.close();
		return fn;
	}

	public void run(LWThread thr) {
        int i = thr.checkSliceAndPopState();
        int fc = thr.popIntState();
        Stack stack = thr.getStack();
        int ec = childCount();
        try {
        	switch (i) {
        		case 0:
        			fc = stack.frameCount();
        			addBuffers(stack);
        			i++;
        		default:
        			for (; i <= ec; i++) {
        				try {
        					runChild(i - 1, thr);
       						commitBuffers(stack);
       						exception.setValue(stack, null);
        					break;
        				}
        				catch (ExecutionException e) {
        					stack.dropToFrame(fc);
        					exception.setValue(stack, e);
        					if (i < ec) {
        						addBuffers(stack);
        					}
        				}
        			}
        			i = Integer.MAX_VALUE;
        		case Integer.MAX_VALUE:
        			ExecutionException lastEx = exception.getValue(stack);
        			if (lastEx != null) {
        				throw lastEx;
        			}
        	}
        }
        catch (Yield y) {
            y.getState().push(fc);
            y.getState().push(i);
            throw y;
        }
    }
}