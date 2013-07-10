// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 21, 2005
 */
package org.globus.cog.karajan.compiled.nodes.user;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.NamedValue;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;

/**
 * Call-by-future function
 */
public class CBFFunction extends Function {
	public static final Logger logger = Logger.getLogger(CBFFunction.class);

	@Override
	protected void _return(Scope scope, Signature sig) {
		Var.Channel ret = scope.parent.lookupChannel("...");
		
		ret.append(new NamedValue(null, null, new Scope.CBFUserDef(this)));
	}
}