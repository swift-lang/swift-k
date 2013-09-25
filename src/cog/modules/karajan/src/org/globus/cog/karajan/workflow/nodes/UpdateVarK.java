//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class UpdateVarK extends SetVarK {
	static {
		setVargs(UpdateVarK.class, true);
	}

	
	protected StackFrame getFrame(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = Arg.VARGS.get(stack);
		if (vargs.size() != 2) {
			throw new ExecutionException("Expected name and value");
		}
		String name = TypeUtil.toString(vargs.get(0));
		int frame = stack.getVarFrameFromTop(name);
		if (frame == VariableStack.NO_FRAME || frame == VariableStack.FIRST_FRAME) {
			throw new VariableNotFoundException(name);
		}
		return stack.getFrameFromTop(frame);
	}
}
