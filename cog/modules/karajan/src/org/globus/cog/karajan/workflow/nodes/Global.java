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
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;

public class Global extends SetVar {
	static {
		setArguments(Global.class, new Arg[] { A_NAME, A_NAMES, A_VALUE, Arg.VARGS });
	}
	
	protected StackFrame getFrame(VariableStack stack) {
		return stack.firstFrame();
	}
}
