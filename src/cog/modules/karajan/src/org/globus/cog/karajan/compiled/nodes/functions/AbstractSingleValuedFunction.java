//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Dec 18, 2012
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import org.globus.cog.karajan.analyzer.Param;
import org.globus.cog.karajan.analyzer.Signature;



public abstract class AbstractSingleValuedFunction extends AbstractFunction {

	protected final Signature getSignature() {
		return new Signature(getParams(), returns(channel("...", 1)));
	}

	protected abstract Param[] getParams();
}
