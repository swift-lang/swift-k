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
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

/*
 * Created on Jul 2, 2004
 */
package org.globus.cog.karajan.compiled.nodes;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.apache.log4j.Logger;
import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.CompilationException;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.Var;
import org.globus.cog.karajan.compiled.nodes.user.Lambda;
import org.globus.cog.karajan.parser.WrapperNode;

public class Apply extends InternalFunction {
	public static final Logger logger = Logger.getLogger(Apply.class);
	
	private ArgRef<Lambda> lambda;
	private ChannelRef<Object> c_vargs;
	private ChannelRef<Object> cr_vargs;
	private Map<String, ChannelRef<Object>> channels;
	
	private static int count = 0;

	@Override
	protected Signature getSignature() {
		return new Signature(
				params("lambda", "..."),
				returns(channel("...", DYNAMIC))
		);
	}

	@Override
	protected void resolveChannelReturns(WrapperNode w, Signature sig, Scope scope)
			throws CompilationException {
		super.resolveChannelReturns(w, sig, scope);
		Collection<String> cnames = scope.getAllChannelNames();
		channels = new HashMap<String, ChannelRef<Object>>();
		for (String c : cnames) {
			Var.Channel vc = scope.lookupChannel(c, this);
			channels.put(c, scope.getChannelRef(vc));
		}
	}

	@Override
	public void run(LWThread thr) throws ExecutionException {
		super.run(thr);
	}

	@SuppressWarnings("unchecked")
	public void runBody(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		Lambda node = (Lambda) thr.popState();
		List<ChannelRef<Object>> cl = (List<ChannelRef<Object>>) thr.popState();
		k.rt.Channel<Object> args = null;
		Stack stack = thr.getStack();
		try {
			if (i == 0) {
				node = this.lambda.getValue(stack);
				cl = new LinkedList<ChannelRef<Object>>();
				for (String name : node.getChannelReturns().keySet()) {
					cl.add(channels.get(name));
				}
				args = c_vargs.get(stack);
				i++;
			}
			node.runBody(thr, cl, args);
		}
		catch (Yield y) {
			y.getState().push(cl);
			y.getState().push(node);
			y.getState().push(i);
			throw y;
		}
	}
}