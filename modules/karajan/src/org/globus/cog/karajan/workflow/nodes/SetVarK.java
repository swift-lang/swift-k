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

import java.util.List;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.StackFrame;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Identifier;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.events.NotificationEvent;
import org.globus.cog.karajan.workflow.events.NotificationEventType;

public class SetVarK extends SequentialWithArguments {

	static {
		setArguments(SetVarK.class, new Arg[] { Arg.VARGS });
	}

	public void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		stack.setVar("#quoted", true);
	}

	public void post(VariableStack stack) throws ExecutionException {
		VariableArguments vargs = Arg.VARGS.get(stack);
		if (vargs.size() == 0) {
			throw new ExecutionException("Missing identifier(s)");
		}
		Object ident = vargs.removeFirst();
		if (ident instanceof List) {
			setMultiple((List) ident, vargs, stack);
		}
		else if (ident instanceof Identifier) {
			setSingle((Identifier) ident, vargs, stack);
		}
		else {
			throw new ExecutionException(
					"First argument must be an identifier or a list of identifiers");
		}
		super.post(stack);
	}

	protected final void setMultiple(List idents, VariableArguments vargs, VariableStack stack)
			throws ExecutionException {
		if (idents.size() != vargs.size()) {
			throw new ExecutionException("Argument size mismatch. Got " + idents.size()
					+ " names and " + vargs.size() + " values");
		}
		for (int i = 0; i < idents.size(); i++) {
			Object next = idents.get(i);
			if (next instanceof Identifier) {
				getFrame(stack).setVar(checkName(((Identifier) next).getName()), vargs.get(i));
			}
			else {
				throw new ExecutionException(
						"Expected a list of identifiers. The element with index " + i
								+ " in the list is not.");
			}
		}
	}

	protected final void setSingle(Identifier ident, VariableArguments vargs, VariableStack stack)
			throws ExecutionException {
		if (vargs.size() != 1) {
			throw new ExecutionException("Got one name and " + vargs.size() + " values: " + vargs);
		}
		else {
			getFrame(stack).setVar(checkName(((Identifier) ident).getName()), vargs.get(0));
		}
	}

	private String checkName(String name) throws ExecutionException {
		if (name.startsWith("#")) {
			throw new ExecutionException("Invalid character ('#') in identifier: " + name);
		}
		return name;
	}

	protected StackFrame getFrame(VariableStack stack) {
		return stack.parentFrame();
	}

	protected void notificationEvent(NotificationEvent e) throws ExecutionException {
		if (e.getType().equals(NotificationEventType.EXECUTION_COMPLETED)) {
			e.getStack().currentFrame().deleteVar("#quoted");
		}
		super.notificationEvent(e);
	}
}