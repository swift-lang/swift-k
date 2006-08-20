// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Sep 27, 2004
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;

public class VargsK extends SequentialWithArguments {
	public static final Logger logger = Logger.getLogger(VargsK.class);

	public static final Arg A_ARGUMENTS = new Arg.Positional("arguments");
	
	static {
		setArguments(VargsK.class, new Arg[] { A_ARGUMENTS });
	}

	public final void post(VariableStack stack) throws ExecutionException {
		VariableArguments vargs;
		if (A_ARGUMENTS.isPresent(stack)) {
			Iterator i = TypeUtil.toIterator(A_ARGUMENTS.getValue(stack));
			while (i.hasNext()) {
				Object next = i.next();
				ArgUtil.getVariableReturn(stack).append(next);
			}
		}
		else {
			vargs = (VariableArguments) stack.getVar("...");
			ArgUtil.getVariableReturn(stack).merge(vargs);
		}
		super.post(stack);
	}
}