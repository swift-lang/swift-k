// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 7, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.DynamicScope;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.parser.WrapperNode;


public class For extends InternalFunction {
	private String name;
	private ArgRef<Iterable<Object>> in;
	private Node body;
	
	private VarRef<Object> var;

	@Override
	protected Signature getSignature() {
		return new Signature(params(identifier("name"), "in", block("body")));
	}

	@Override
	protected void runBody(LWThread thr) {		
		int i = thr.checkSliceAndPopState();
		@SuppressWarnings("unchecked")
		Iterator<Object> it = (Iterator<Object>) thr.popState();
		Stack stack = thr.getStack();
		try {
			out: {
				while (true) {
					switch (i) {
						case 0:
							it = in.getValue(stack).iterator();
							i++;
						case 1:
							if (it.hasNext()) {
								var.setValue(stack, it.next());
								i++;
							}
							else {
								break out;
							}
						case 2:
							if (CompilerSettings.PERFORMANCE_COUNTERS) {
								startCount++;
							}
							body.run(thr);
							i = 1;
					}
				}
			}
		}
		catch (Yield y) {
			y.getState().push(it);
			y.getState().push(i);
			throw y;
		}
	}

	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		Var v = scope.addVar(name);
		var = scope.getVarRef(v);
		DynamicScope ds = new DynamicScope(w, scope);
		super.compileBlocks(w, sig, blocks, ds);
		ds.close();
	}

	@Override
	public void dump(PrintStream ps, int level) throws IOException {
		super.dump(ps, level);
		if (body != null) {
			body.dump(ps, level + 1);
		}
	}
}
