// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.workflow.nodes;

import org.globus.cog.karajan.arguments.Arg;
import org.globus.cog.karajan.stack.VariableStack;
import org.globus.cog.karajan.util.Cache;
import org.globus.cog.karajan.workflow.ExecutionException;

public class Once extends CacheNode {
	public static final String CACHE = "##cache";

	public static final Arg A_VALUE = new Arg.Positional("value", 0);

	static {
		setArguments(Once.class, new Arg[] { A_VALUE });
	}

	protected void partialArgumentsEvaluated(VariableStack stack) throws ExecutionException {
		cpre(A_VALUE.getValue(stack), stack);
	}

	protected synchronized Cache getCache(VariableStack stack) throws ExecutionException {
		Cache cache = (Cache) stack.getGlobal(CACHE);
		if (cache == null) {
			cache = new Cache();
			cache.setMaxCacheSize(-1);
			stack.setGlobal(CACHE, cache);
		}
		return cache;
	}
}
