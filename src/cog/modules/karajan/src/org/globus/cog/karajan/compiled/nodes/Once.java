// ----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Apr 26, 2005
 */
package org.globus.cog.karajan.compiled.nodes;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;

import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.util.Cache;

public class Once extends CacheNode {
	public static final String CACHE = "#once#cache";
	
	@Override
	protected Signature getSignature() {
		return new Signature(params("on", block("body")));
	}
	
	protected Cache getCache(Stack stack, boolean staticdef) throws ExecutionException {
		Context ctx = this.context.getValue(stack);
		synchronized(ctx) {
			Cache c = (Cache) ctx.getAttribute(CACHE);
			if (c == null) {
				c = new Cache();
				c.setMaxCacheSize(-1);
				ctx.setAttribute(CACHE, c);
			}
			return c;
		}
	}
}
