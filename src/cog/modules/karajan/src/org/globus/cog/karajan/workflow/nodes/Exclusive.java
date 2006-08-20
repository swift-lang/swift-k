// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import java.util.LinkedList;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Exclusive extends Sequential {
	private final LinkedList instances;
	private boolean locked;

	public Exclusive() {
		instances = new LinkedList();
	}

	protected synchronized void pre(VariableStack stack) throws ExecutionException {
		super.pre(stack);
		if (locked) {
			instances.addFirst(stack);
		}
		else {
			locked = true;
			this.executeChildren(stack);
		}
	}

	protected synchronized void _finally(VariableStack stack) throws ExecutionException {
		locked = false;
		if (instances.size() > 0) {
			VariableStack other = (VariableStack) instances.removeLast();
			pre(other);
		}
		super._finally(stack);
	}
}
