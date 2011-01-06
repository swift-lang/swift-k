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

	protected synchronized void setSimple(boolean simple) {
		this.simple = simple;
	}

	protected synchronized void setSimple(Object value) {
		this.value = value;
		this.simple = true;
	}

	public synchronized boolean isSimple() {
		return simple;
	}
	
	private synchronized Object getValue() {
		return simple ? value : null;
	}

	public void executeSimple(final VariableStack stack) throws ExecutionException {
		startCount++;
		stack.enter();
		initializeArgs(stack);
		Object v = getValue();
		if (v != null) {
			ret(stack, v);
		}
		else {
			ret(stack, function(stack));
		}
		stack.leave();
	}
}