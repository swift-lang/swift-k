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
 * Created on Nov 6, 2003
 */
package org.globus.cog.karajan.compiled.nodes;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import k.rt.ExecutionException;
import k.rt.Stack;
import k.rt.WaitYield;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ArgRef;
import org.globus.cog.karajan.analyzer.Signature;

public class WaitNode extends InternalFunction {
	private ArgRef<Number> delay;
	private ArgRef<String> until;
		
	@Override
	protected Signature getSignature() {
		return new Signature(params(optional("delay", null), optional("until", null)));
	}

	@Override
	protected void runBody(LWThread thr) {
		int i = thr.checkSliceAndPopState(1);
		Stack stack = thr.getStack();
		try {
			switch (i) {
				case 0:
					wait(thr, stack);
					break;
				default:
			}
		}
		catch (WaitYield y) {
			throw y;
		}
		catch (Yield y) {
			y.getState().push(i, 1);
			throw y;
		}
	}
		
	private void wait(LWThread thr, Stack stack) {
		Number delay = this.delay.getValue(stack);
		if (delay != null) {
			throw new WaitYield(1, 1, delay.intValue());
		}
		String until = this.until.getValue(stack);
		if (until != null) {
			try {
				throw new WaitYield(1, 1, DateFormat.getDateTimeInstance().parse(until));
			}
			catch (ParseException e) {
				try {
					Calendar cal = Calendar.getInstance();
					cal.setTime(DateFormat.getDateInstance().parse(until));
					cal.set(Calendar.HOUR, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					throw new WaitYield(1, 2, DateFormat.getDateInstance().parse(until));
				}
				catch (ParseException e1) {
					try {
						Calendar now = Calendar.getInstance();
						Calendar cal = Calendar.getInstance();
						cal.setTime(DateFormat.getTimeInstance().parse(until));
						cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
						cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
						cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
						if (cal.before(now)) {
							cal.add(Calendar.DAY_OF_MONTH, 1);
						}
						throw new WaitYield(1, 2, cal.getTime());
					}
					catch (ParseException e2) {
						throw new ExecutionException(this, "Could not parse date/time: " + until, e);
					}
				}
			}
		}
		throw new ExecutionException(this, "Missing both 'delay' and 'until' parameters");
	}
}