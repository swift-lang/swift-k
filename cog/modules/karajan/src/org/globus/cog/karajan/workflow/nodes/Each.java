//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 27, 2006
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.Iterator;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Each extends AbstractSequentialWithArguments {
	public static final Arg A_ITEMS = new Arg.Positional("items", 0);

	static {
		setArguments(Each.class, new Arg[] { A_ITEMS });
	}

	protected void post(VariableStack stack) throws ExecutionException {
		Iterator i = TypeUtil.toIterator(A_ITEMS.getValue(stack));
		while (i.hasNext()) {
			ret(stack, i.next());
		}
		super.post(stack);
	}

}
