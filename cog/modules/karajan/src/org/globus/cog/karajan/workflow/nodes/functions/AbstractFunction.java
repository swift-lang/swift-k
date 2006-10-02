// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 8, 2003
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import org.globus.cog.karajan.arguments.ArgUtil;
import org.globus.cog.karajan.arguments.VariableArguments;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.SequentialWithArguments;

public abstract class AbstractFunction extends SequentialWithArguments {
	private Object value;
	private boolean simple;
	
	public AbstractFunction() {
		setAcceptsInlineText(true);
	}

	public void pre(VariableStack stack) throws ExecutionException {
		if (simple) {
			ret(stack, value);
		}
		else {
			super.pre(stack);
		}
	}

	protected void executeChildren(VariableStack stack) throws ExecutionException {
		if (simple) {
			complete(stack);
		}
		else {
			super.executeChildren(stack);
		}
	}

	public final void post(VariableStack stack) throws ExecutionException {
		ret(stack, function(stack));
		super.post(stack);
	}

	protected void ret(VariableStack stack, final Object value) throws ExecutionException {
		if (value != null) {
			final VariableArguments vret = ArgUtil.getVariableReturn(stack);
			if (value.getClass().isArray()) {
				try {
					Object[] array = (Object[]) value;
					for (int i = 0; i < array.length; i++) {
						vret.append(array[i]);
					}
				}
				catch (ClassCastException e) {
					// array of primitives; return as is
					vret.append(value);
				}
			}
			else {
				vret.append(value);
			}
		}
	}

	public abstract Object function(VariableStack stack) throws ExecutionException;

	protected void setSimple(boolean simple) {
		this.simple = simple;
	}

	protected void setValue(Object value) {
		this.value = value;
	}

	public boolean isSimple() {
		return simple;
	}

	public void executeSimple(final VariableStack stack) throws ExecutionException {
		startCount++;
		stack.enter();
		initializeArgs(stack);
		if (value != null) {
			ret(stack, value);
		}
		else {
			ret(stack, function(stack));
		}
		stack.leave();
	}
}