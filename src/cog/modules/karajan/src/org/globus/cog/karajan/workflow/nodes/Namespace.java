// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.NonCacheable;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Namespace extends PartialArgumentsContainer implements NonCacheable {
	public static final Arg A_PREFIX = new Arg.Positional("prefix", 0);
	
	static {
		setArguments(Namespace.class, new Arg[] {A_PREFIX});
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		stack.setVar("#namespaceprefix", A_PREFIX.getValue(stack));
		super.partialArgumentsEvaluated(stack);
		startRest(stack);
	}

	public void post(VariableStack stack) throws ExecutionException {
		Iterator i = stack.currentFrame().names().iterator();
		while (i.hasNext()) {
			String name = (String) i.next();
			if (name.startsWith("#def#")) {
				stack.parentFrame().setVar(name, stack.currentFrame().getVar(name));
			}
		}
		super.post(stack);
	}	
}