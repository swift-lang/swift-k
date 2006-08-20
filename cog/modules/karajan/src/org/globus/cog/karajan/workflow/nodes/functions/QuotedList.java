// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;

public class QuotedList extends SequentialWithArguments { 
	static {
		setVargs(QuotedList.class, true);
	}
	
	private Object list;
	
	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		stack.setVar("#quoted", true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = ArgUtil.getVariableArguments(stack);
		//TODO check if all children are static
		ArgUtil.getVariableReturn(stack).append(vargs.getAll());
		super.post(stack);
	}
	
	public boolean isSimple() {
		return list != null;
	}
	
	
	public void executeSimple(VariableStack stack) throws ExecutionException {
		ArgUtil.getVariableReturn(stack).append(list);
	}
}