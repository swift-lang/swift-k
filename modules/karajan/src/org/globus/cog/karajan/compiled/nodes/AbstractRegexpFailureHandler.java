//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Nov 24, 2006
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.regex.Pattern;

import k.rt.ExecutionException;

import org.apache.log4j.Logger;

public abstract class AbstractRegexpFailureHandler extends InternalFunction {
	public static final Logger logger = Logger.getLogger(AbstractRegexpFailureHandler.class);
	
	protected static boolean matches(String str, String msg) {
		if (msg == null) {
			msg = "";
		}
		boolean matches = Pattern.compile(str, Pattern.DOTALL).matcher(msg).matches();
		if (!matches && logger.isDebugEnabled()) {
			logger.debug("Failure does not match: \"" + msg + "\" vs. " + str);
		}
		return matches;
	}
	
	protected static boolean matches(String str, ExecutionException e) {
		return matches(str, e.getMessage());
	}
}
