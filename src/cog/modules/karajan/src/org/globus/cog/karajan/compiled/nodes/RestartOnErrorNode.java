// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 23, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.parser.WrapperNode;

public class RestartOnErrorNode extends AbstractRegexpFailureHandler {
	private ArgRef<String> match;
	private ArgRef<Number> times;
	private Node body;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(optional("match", null), "times", block("body")));
	}


	@Override
	protected Node compileBody(WrapperNode w, Scope argScope, Scope scope)
			throws CompilationException {
		if (times.getValue() != null && times.getValue().intValue() == 0) {
			return body;
		}
		else {
			return super.compileBody(w, argScope, scope);
		}
	}

	protected void runBody(LWThread thr) {
        int i = thr.checkSliceAndPopState();
        int fc = thr.popIntState();
        int times = thr.popIntState();
        Stack stack = thr.getStack();
        int ec = childCount();
        if (i == 0) {
            fc = stack.frameCount();
            times = this.times.getValue(stack).intValue();
            i++;
        }
        while (true) {
	        try {
	        	if (CompilerSettings.PERFORMANCE_COUNTERS) {
					startCount++;
				}
	            body.run(thr);
	            break;
	        }
	        catch (Yield y) {
	        	y.getState().push(times);
	        	y.getState().push(fc);
	            y.getState().push(i);
	            throw y;
	        }
	        catch (ExecutionException e) {
	        	stack.dropToFrame(fc);
	        	String match = this.match.getValue(stack);
	            if (match == null || matches(match, e)) {
	                times--;
	            }
	            if (times < 0) {
	                throw e;
	            }
	        }
        }
    }
}
