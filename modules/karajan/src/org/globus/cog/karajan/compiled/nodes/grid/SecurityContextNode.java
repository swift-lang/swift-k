// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 31, 2003
 */
package org.globus.cog.karajan.compiled.nodes.grid;

import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.abstraction.impl.common.AbstractionFactory;
import org.globus.cog.abstraction.interfaces.SecurityContext;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.functions.AbstractFunction;
import org.globus.cog.karajan.util.TypeUtil;

public class SecurityContextNode extends AbstractFunction {
	
	private ArgRef<String> provider;
	private ArgRef<Object> credentials;


	@Override
	protected Signature getSignature() {
		return new Signature(params("provider", "credentials"));
	}



	public Object function(Stack stack) {
		SecurityContext context = null;
		if (context == null) {
		    String provider = TypeUtil.toString(this.provider.getValue(stack));
			try {
				context = AbstractionFactory.newSecurityContext(provider);
			}
			catch (Exception e) {
				throw new ExecutionException("Unsupported security context type: " + provider, e);
			}
			context.setCredentials(credentials.getValue(stack));
		}
		return context;
	}
}