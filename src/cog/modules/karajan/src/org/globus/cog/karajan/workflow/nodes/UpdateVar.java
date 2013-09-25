// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jun 6, 2003
 *  
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class UpdateVar extends SetVar {
	static {
		setArguments(UpdateVar.class, new Arg[] { A_NAME, A_NAMES, A_VALUE, Arg.VARGS });
	}
	
	protected StackFrame getFrame(VariableStack stack) throws ExecutionException {
		String name = (String) A_NAME.getValue(stack);
		int frame = stack.getVarFrameFromTop(name);
		if (frame == VariableStack.NO_FRAME || frame == VariableStack.FIRST_FRAME) {
			throw new VariableNotFoundException(name);
		}
		return stack.getFrameFromTop(frame);
	}
}