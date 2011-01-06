// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * 
 * Created on Mar 27, 2004
 *  
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.SingleValueVariableArguments;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;

public class If extends Sequential {
	public static final String COMPLETE = "##complete";

	protected void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		ArgUtil.setVariableArguments(stack, new SingleValueVariableArguments());
	}

	protected void childCompleted(VariableStack stack) throws ExecutionException {
		if (stack.currentFrame().isDefined(COMPLETE)) {
			complete(stack);
			return;
		}
		int index = getIndex(stack);
		if ((index & 1) == 1) {
			boolean condition = false;
			try {
				condition = TypeUtil.toBoolean(ArgUtil.getVariableArguments(stack).removeFirst());
			}
			catch (IndexOutOfBoundsException e) {
				throw new ExecutionException("No condition specified");
			}
			if (condition) {
				ArgUtil.removeVariableArguments(stack);
				stack.setVar(COMPLETE, true);
				startNext(stack);
			}
			else {
				// skip one if not last
				index++;
				preIncIndex(stack);
				if (index == this.elementCount() - 1) {
					stack.setVar(COMPLETE, true);
					ArgUtil.removeVariableArguments(stack);
					startNext(stack);
				}
				else if (index < this.elementCount()) {
					startNext(stack);
				}
				else {
					complete(stack);
				}
			}
		}
		else {
			if (index == this.elementCount() - 1) {
				stack.setVar(COMPLETE, true);
			}
			else {
				ArgUtil.setVariableArguments(stack, new SingleValueVariableArguments());
			}
			startNext(stack);
		}
	}
}
