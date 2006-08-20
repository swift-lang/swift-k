// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Feb 21, 2005
 */
package org.globus.cog.karajan.parser.atoms;

import org.globus.cog.karajan.parser.EvaluationContext;
import org.globus.cog.karajan.parser.EvaluationException;
import org.globus.cog.karajan.stack.VariableNotFoundException;
import org.globus.cog.karajan.workflow.futures.Future;

public abstract class AbstractEvaluator implements Evaluator {
	private Object[] children;

	public AbstractEvaluator() {
	}

	public AbstractEvaluator(int count) {
		children = new Object[count];
	}

	public void initializeChildren(int size) {
		children = new Object[size];
	}

	protected int childCount() {
		if (children == null) {
			return 0;
		}
		else {
			return children.length;
		}
	}

	protected void setChild(int index, Object value) {
		children[index] = value;
	}

	protected Object getChild(int index) {
		return children[index];
	}

	protected Object[] getChildren() {
		return children;
	}

	protected Object evalChild(final int index, final EvaluationContext table) throws EvaluationException {
		Object obj;
		if (children[index] instanceof Evaluator) {
			obj = ((Evaluator) children[index]).evaluate(table);
		}
		else {
			obj = children[index];
		}
		if (obj instanceof Future) {
			try {
				obj = ((Future) obj).getValue();
			}
			catch (VariableNotFoundException e) {
				throw new EvaluationException(e);
			}
		}
		return obj;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append('(');
		for (int i = 0; i < children.length; i++) {
			if (i != 0) {
				sb.append(", ");
			}
			if (children[i] == null) {
				sb.append('?');
			}
			else {
				if (children[i] instanceof String) {
					sb.append('\'');
					sb.append(children[i]);
					sb.append('\'');
				}
				else {
					sb.append(children[i].toString());
				}
			}
		}
		sb.append(')');
		return sb.toString();
	}
}