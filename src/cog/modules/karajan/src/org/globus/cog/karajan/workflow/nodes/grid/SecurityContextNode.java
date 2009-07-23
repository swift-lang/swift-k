// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.workflow.nodes.grid;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.TypeUtil;
import org.globus.cog.karajan.workflow.ExecutionException;
import org.globus.cog.karajan.workflow.nodes.functions.AbstractFunction;

public class SecurityContextNode extends AbstractFunction {
	public static final Arg A_PROVIDER = new Arg.Positional("provider");
	public static final Arg A_CREDENTIALS = new Arg.Positional("credentials");

	static {
		setArguments(SecurityContextNode.class, new Arg[] { A_PROVIDER, A_CREDENTIALS });
	}

	public Object function(VariableStack stack) throws ExecutionException {
		SecurityContext context = (SecurityContext) checkClass(
				stack.getExecutionContext().getAttribute("securityContext"), SecurityContext.class,
				"SecurityContext");
		if (context == null) {
		    String provider = TypeUtil.toString(A_PROVIDER.getValue(stack));
			try {
				context = AbstractionFactory.newSecurityContext(provider);
			}
			catch (Exception e) {
				throw new ExecutionException("Unsupported security context type: " + provider, e);
			}
			context.setCredentials(A_CREDENTIALS.getValue(stack));
		}
		return context;
	}
}