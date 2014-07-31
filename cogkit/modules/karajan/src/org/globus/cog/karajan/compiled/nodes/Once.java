/*
 * Swift Parallel Scripting Language (http://swift-lang.org)
 * Code from Java CoG Kit Project (see notice below) with modifications.
 *
 * Copyright 2005-2014 University of Chicago
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
