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
 * Created on Feb 19, 2005
 */
package org.globus.cog.karajan.compiled.nodes.functions;

import java.util.List;

import k.rt.ChannelOperator;
import k.rt.Stack;
import k.thr.LWThread;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;

public class Sum extends InternalFunction {
	
	private ChannelRef<Number> c_vargs;
	private ChannelRef<Object> cr_vargs;
	private double initial;

	@Override
	protected Signature getSignature() {
		return new Signature(params("..."), returns(channel("...", 1)));
	}
	
	private static class Op extends ChannelOperator.Double<Number> {
		public Op() {
			super(0);
		}
		
		public Op(double value) {
			super(value);
		}
		
		@Override
		protected double update(double crt, Number value) {
			return crt + value.doubleValue();
		}

		@Override
		public String toString() {
			return "sumop";
		}
	}

	@Override
	protected void initializeArgs(Stack stack) {
		c_vargs.set(stack, new Op(initial));
	}
	
	@Override
	protected ChannelRef<?> makeStaticChannel(int index, List<Object> values) {
		sumStatic(values);
		return new ChannelRef.Static<Number>(new Op(initial));
	}
	
	private void sumStatic(List<Object> values) {
		initial = 0;
		for (Object o : values) {
			if (o instanceof Number) {
				initial += ((Number) o).doubleValue();
			}
			else {
				throw new IllegalArgumentException("Not a number: " + o);
			}
		}
	}

	@Override
	protected ChannelRef<?> makeMixedChannel(int index, List<Object> staticValues) {
		sumStatic(staticValues);
		return new ChannelRef.Dynamic<Object>("+", index);
	}

	@Override
	protected ChannelRef<Object> makeDynamicChannel(String name, int index) {
		return new ChannelRef.Dynamic<Object>("+", index);
	}

	@Override
	protected void runBody(LWThread thr) {
		Stack stack = thr.getStack();
		cr_vargs.append(stack, ((ChannelOperator.Double<?>) c_vargs.get(stack)).getValue());
	}
}