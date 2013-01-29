// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;

import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.CompilerSettings;
import org.globus.cog.karajan.analyzer.Pure;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.parser.WrapperNode;

public class Namespace extends InternalFunction implements Pure {
	public static final String VAR_NAME = "#namespaceprefix";
	
	private String prefix;
	private Node body;
	

	@Override
	protected Signature getSignature() {
		return new Signature(
				params(identifier("prefix"), block("body"))
		);
	}
	
	@Override
	protected void compileBlocks(WrapperNode w, Signature sig, LinkedList<WrapperNode> blocks,
			Scope scope) throws CompilationException {
		scope.addVar(VAR_NAME, prefix);
		super.compileBlocks(w, sig, blocks, scope);
	}

	@Override
	protected void runBody(LWThread thr) {
		if (body != null) {
			if (CompilerSettings.PERFORMANCE_COUNTERS) {
				startCount++;
			}
			body.run(thr);
		}
	}

	@Override
	public void dump(PrintStream ps, int level) throws IOException {
		super.dump(ps, level);
		if (body != null) {
			body.dump(ps, level + 1);
		}
	}
}