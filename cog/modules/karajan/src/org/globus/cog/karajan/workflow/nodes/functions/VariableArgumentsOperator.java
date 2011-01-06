//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 21, 2005
 */
package org.globus.cog.karajan.workflow.nodes.functions;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.arguments.AbstractWriteOnlyVariableArguments;
import org.globus.cog.karajan.arguments.VariableArguments;

public abstract class VariableArgumentsOperator extends AbstractWriteOnlyVariableArguments {
	private Object value;

	protected VariableArgumentsOperator() {
		value = initialValue();
	}

	protected abstract Object initialValue();

	protected abstract Object update(Object oldvalue, Object item);

	protected Object getValue() {
		return value;
	}

	public void merge(VariableArguments args) {
		appendAll(args.getAll());
	}

	public synchronized void append(Object value) {
		this.value = update(this.value, value);
	}

	public void appendAll(List args) {
		Iterator i = args.iterator();
		while (i.hasNext()) {
			append(i.next());
		}
	}
}
