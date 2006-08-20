//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 2, 2006
 */
package org.globus.cog.karajan.workflow.nodes.user;

import java.util.Iterator;
import java.util.List;

import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.DefUtil;
import org.globus.cog.karajan.util.DefinitionEnvironment;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.FlowNode;

public class Self extends UDEWrapper {

	protected UDEDefinition getDefInternal(VariableStack stack) throws ExecutionException {
		List l = stack.getAllVars(CALLER);
		Iterator i = l.iterator();
		while (i.hasNext()) {
			FlowNode n = (FlowNode) i.next();
			if (n instanceof UserDefinedElement) {
				return new UDEDefinition(n, (DefinitionEnvironment) stack.getShallowVar(DefUtil.ENV));
			}
		}
		throw new ExecutionException("No enclosing element definition found");
	}
}
