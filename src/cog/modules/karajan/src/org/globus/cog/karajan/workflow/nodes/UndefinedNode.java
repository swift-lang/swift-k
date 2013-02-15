
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

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.workflow.ExecutionException;


public class UndefinedNode extends Sequential {
	public boolean supportsType(String type) {
		return true;
	}

	public void pre(VariableStack vs) throws ExecutionException {
		fail(vs, "Unknown element: "+getElementType()+". Maybe you misspelled it.");
	}
}
