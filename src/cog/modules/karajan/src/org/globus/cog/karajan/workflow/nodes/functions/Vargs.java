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
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.AbstractSequentialWithArguments;

public class Vargs extends AbstractSequentialWithArguments {
	private static final Logger logger = Logger.getLogger(Vargs.class);

	public static final Arg A_ARGUMENTS = new Arg.Optional("arguments");

	static {
		setArguments(Vargs.class, new Arg[] { A_ARGUMENTS });
	}

	public final void post(VariableStack stack) throws ExecutionException {
		VariableArguments vargs;
		if (A_ARGUMENTS.isPresent(stack)) {
			Iterator i = TypeUtil.toIterator(A_ARGUMENTS.getValue(stack));
			while (i.hasNext()) {
				Object next = i.next();
				Arg.VARGS.getReturn(stack).append(next);
			}
		}
		else {
			vargs = (VariableArguments) stack.getVar("vargs");
			Arg.VARGS.getReturn(stack).merge(vargs);
		}
		super.post(stack);
	}
}