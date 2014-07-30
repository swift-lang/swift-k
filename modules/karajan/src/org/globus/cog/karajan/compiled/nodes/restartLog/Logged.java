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

//----------------------------------------------------------------------
//This code is developed as part of the Java CoG Kit project
//The terms of the license can be found at http://www.cogkit.org/license
//This message may not be removed or altered.
//----------------------------------------------------------------------

/*
 * Created on Mar 22, 2006
 */
package org.globus.cog.karajan.compiled.nodes.restartLog;

import java.util.Map;

import k.rt.Context;
import k.rt.ExecutionException;
import k.rt.Stack;
import k.thr.LWThread;
import k.thr.Yield;

import org.globus.cog.karajan.analyzer.ChannelRef;
import org.globus.cog.karajan.analyzer.Scope;
import org.globus.cog.karajan.analyzer.Signature;
import org.globus.cog.karajan.analyzer.VarRef;
import org.globus.cog.karajan.analyzer.VariableNotFoundException;
import org.globus.cog.karajan.compiled.nodes.InternalFunction;

public class Logged extends InternalFunction {
	private ChannelRef<String> cr_restartLog;
	private VarRef<Context> context;
	
	@Override
	protected Signature getSignature() {
		return new Signature(params(), returns(channel("restartLog")));
	}
	
	@Override
	protected void addLocals(Scope scope) {
	    context = scope.getVarRef("#context");
		super.addLocals(scope);
	}

	@Override
	public void run(LWThread thr) {
		int i = thr.checkSliceAndPopState();
		try {
			Stack stack = thr.getStack();
			switch (i) {
				case 0:
					if (checkLogged(thr, stack)) {
						return;
					}
					i++;
				case 1:
					super.run(thr);
					cr_restartLog.append(stack, LogEntry.build(thr, this).toString());
			}
		}
		catch (Yield y) {
			y.getState().push(i);
			throw y;
		}
	}

	protected boolean checkLogged(LWThread thr, Stack stack) {
		try {
			@SuppressWarnings("unchecked")
			Map<LogEntry, Object> map = (Map<LogEntry, Object>) context.getValue(stack).getAttribute(RestartLog.LOG_DATA);
			LogEntry entry = LogEntry.build(thr, this);
			boolean found = false;
			synchronized(map) {
				MutableInteger count = (MutableInteger) map.get(entry);
				if (count != null && count.getValue() > 0) {
					count.dec();
					found = true;
				}
			}
			return found;
		}
		catch (VariableNotFoundException e) {
			throw new ExecutionException("No restart log environment found");
		}
	}
}
