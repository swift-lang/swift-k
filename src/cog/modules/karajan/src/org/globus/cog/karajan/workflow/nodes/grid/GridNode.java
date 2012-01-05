
// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.karajan.workflow.nodes.grid;

import java.util.Iterator;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.BoundContact;
import org.globus.cog.karajan.util.ContactSet;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;


public class GridNode extends AbstractFunction {
	static {
		setArguments(GridNode.class, new Arg[] {Arg.VARGS});
	}
	
	public Object function(VariableStack stack) throws ExecutionException {
		ContactSet grid = new ContactSet();
		Iterator i = Arg.VARGS.asList(stack).iterator();
		while (i.hasNext()) {
			Object o = i.next();
			if (!(o instanceof BoundContact)) {
				throw new ExecutionException("Unexpected argument "+o);
			}
			grid.addContact((BoundContact) o);
		}
		return grid;
	}
}
